package com.employment.service;

import com.employment.common.BizException;
import com.employment.common.PageResult;
import com.employment.entity.AuditLog;
import com.employment.entity.Job;
import com.employment.entity.JobApplication;
import com.employment.mapper.JobApplicationMapper;
import com.employment.mapper.JobMapper;
import com.employment.query.ApplicationQuery;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.employment.service.ApplicationFlow.Action;

/**
 * 岗位申请业务：学生申请 + 负责人端初审、面试、录用登记。
 *
 * <p>所有状态变更都经过 {@link ApplicationFlow} 校验，并写入 {@code audit_log}，
 * 以保证状态流转合法、处理过程可追溯。
 */
@Service
public class ApplicationService {

    private final JobApplicationMapper applicationMapper;
    private final JobMapper jobMapper;
    private final AuditService auditService;

    public ApplicationService(JobApplicationMapper applicationMapper, JobMapper jobMapper,
                             AuditService auditService) {
        this.applicationMapper = applicationMapper;
        this.jobMapper = jobMapper;
        this.auditService = auditService;
    }

    // ==================== 学生端 ====================

    /**
     * 学生申请岗位。
     *
     * <p>申请前依次校验：岗位存在、岗位仍在招聘、本人未申请过同一岗位。
     *
     * @return 返回 null 表示成功，否则返回错误提示信息。
     */
    public String apply(Long jobId, Long studentId) {
        if (studentId == null) {
            return "请先登录后再申请岗位";
        }
        Job job = jobMapper.findById(jobId);
        if (job == null) {
            return "岗位不存在或已被删除";
        }
        if (!job.isOpen()) {
            return "该岗位「" + job.getStatus() + "」，已停止招聘，不能申请";
        }
        if (applicationMapper.findByJobAndStudent(jobId, studentId) != null) {
            return "您已申请过该岗位，请勿重复申请";
        }
        applicationMapper.insert(jobId, studentId);
        JobApplication created = applicationMapper.findByJobAndStudent(jobId, studentId);
        if (created != null) {
            auditService.record(AuditService.TYPE_APPLICATION, created.getId(),
                    "SUBMIT", "学生提交申请", null, "待审核 / 未安排 / 待定",
                    studentId, "学生本人", null);
        }
        return null;
    }

    /** 当前学生是否已申请过该岗位。 */
    public boolean hasApplied(Long jobId, Long studentId) {
        if (jobId == null || studentId == null) {
            return false;
        }
        return applicationMapper.findByJobAndStudent(jobId, studentId) != null;
    }

    /** 当前学生已申请的岗位 ID 集合（列表页用于标记「已申请」）。 */
    public Set<Long> myAppliedJobIds(Long studentId) {
        if (studentId == null) {
            return Set.of();
        }
        return applicationMapper.findByStudent(studentId).stream()
                .map(JobApplication::getJobId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public List<JobApplication> myApplications(Long studentId) {
        return applicationMapper.findByStudent(studentId);
    }

    /** 学生查看本人某条申请的完整进度（含归属校验）。 */
    public JobApplication myDetail(Long id, Long studentId) {
        JobApplication application = detail(id);
        if (studentId == null || !studentId.equals(application.getStudentId())) {
            throw new BizException("无权查看他人的申请记录");
        }
        return application;
    }

    // ==================== 负责人端 ====================

    /** 条件分页查询。 */
    public PageResult<JobApplication> search(ApplicationQuery query) {
        long total = applicationMapper.count(query);
        List<JobApplication> items = total == 0 ? List.of() : applicationMapper.search(query);
        return new PageResult<>(items, query.getPage(), query.getSize(), total);
    }

    /** 审核详情。 */
    public JobApplication detail(Long id) {
        JobApplication application = applicationMapper.findDetailById(id);
        if (application == null) {
            throw new BizException("申请记录不存在或已被删除");
        }
        return application;
    }

    /** 导出用：按条件取全部记录（不分页）。 */
    public List<JobApplication> export(ApplicationQuery query) {
        ApplicationQuery all = new ApplicationQuery();
        all.setKeyword(query.getKeyword());
        all.setStatus(query.getStatus());
        all.setFrom(query.getFrom());
        all.setTo(query.getTo());
        all.setPage(1);
        all.setSize(10_000);
        return applicationMapper.search(all);
    }

    /** 查询处理进度时间线。 */
    public List<AuditLog> timeline(Long id) {
        return auditService.timeline(AuditService.TYPE_APPLICATION, id);
    }

    /** 初审通过。 */
    public String approve(Long id, String comment, Long operatorId, String operatorName) {
        JobApplication a = requireForUpdate(id);
        String error = ApplicationFlow.check(Action.APPROVE,
                a.getStatus(), a.getInterviewResult(), a.getHireStatus());
        if (error != null) {
            return error;
        }
        String from = ApplicationFlow.describe(a.getStatus(), a.getInterviewResult(), a.getHireStatus());
        publish(id, ApplicationFlow.PASSED, null, operatorId, operatorName);
        auditService.record(AuditService.TYPE_APPLICATION, id, "APPROVE", Action.APPROVE.getLabel(),
                from, ApplicationFlow.describe(ApplicationFlow.PASSED,
                        a.getInterviewResult(), a.getHireStatus()),
                operatorId, operatorName, blankToNull(comment));
        return null;
    }

    /** 初审不通过（必须填写原因）。 */
    public String reject(Long id, String reason, Long operatorId, String operatorName) {
        if (reason == null || reason.isBlank()) {
            return "初审不通过时必须填写原因";
        }
        JobApplication a = requireForUpdate(id);
        String error = ApplicationFlow.check(Action.REJECT,
                a.getStatus(), a.getInterviewResult(), a.getHireStatus());
        if (error != null) {
            return error;
        }
        String from = ApplicationFlow.describe(a.getStatus(), a.getInterviewResult(), a.getHireStatus());
        publish(id, ApplicationFlow.REJECTED, reason.trim(), operatorId, operatorName);
        auditService.record(AuditService.TYPE_APPLICATION, id, "REJECT", Action.REJECT.getLabel(),
                from, ApplicationFlow.describe(ApplicationFlow.REJECTED,
                        a.getInterviewResult(), a.getHireStatus()),
                operatorId, operatorName, reason.trim());
        return null;
    }

    /** 安排面试。 */
    public String schedule(Long id, LocalDateTime interviewTime, String place,
                           Long operatorId, String operatorName) {
        if (interviewTime == null) {
            return "请填写面试时间";
        }
        JobApplication a = requireForUpdate(id);
        String error = ApplicationFlow.check(Action.SCHEDULE,
                a.getStatus(), a.getInterviewResult(), a.getHireStatus());
        if (error != null) {
            return error;
        }
        String from = ApplicationFlow.describe(a.getStatus(), a.getInterviewResult(), a.getHireStatus());
        LocalDateTime now = LocalDateTime.now();
        applicationMapper.updateInterview(id, interviewTime, blankToNull(place),
                ApplicationFlow.IV_WAIT, operatorId, operatorName, now);
        auditService.record(AuditService.TYPE_APPLICATION, id, "SCHEDULE", Action.SCHEDULE.getLabel(),
                from, ApplicationFlow.describe(ApplicationFlow.PASSED,
                        ApplicationFlow.IV_WAIT, a.getHireStatus()),
                operatorId, operatorName,
                buildInterviewComment(interviewTime, place));
        return null;
    }

    /** 登记面试结果。 */
    public String interviewResult(Long id, boolean passed, String comment,
                                  Long operatorId, String operatorName) {
        JobApplication a = requireForUpdate(id);
        Action action = passed ? Action.INTERVIEW_PASS : Action.INTERVIEW_FAIL;
        String error = ApplicationFlow.check(action,
                a.getStatus(), a.getInterviewResult(), a.getHireStatus());
        if (error != null) {
            return error;
        }
        String result = passed ? ApplicationFlow.IV_PASS : ApplicationFlow.IV_FAIL;
        String from = ApplicationFlow.describe(a.getStatus(), a.getInterviewResult(), a.getHireStatus());
        LocalDateTime now = LocalDateTime.now();
        applicationMapper.updateInterview(id, a.getInterviewTime(), a.getInterviewPlace(),
                result, operatorId, operatorName, now);
        auditService.record(AuditService.TYPE_APPLICATION, id, passed ? "INTERVIEW_PASS" : "INTERVIEW_FAIL",
                action.getLabel(), from,
                ApplicationFlow.describe(a.getStatus(), result, a.getHireStatus()),
                operatorId, operatorName, blankToNull(comment));
        return null;
    }

    /** 登记录用 / 未录用结果。 */
    public String hire(Long id, boolean hired, String comment,
                       Long operatorId, String operatorName) {
        JobApplication a = requireForUpdate(id);
        Action action = hired ? Action.HIRE : Action.NO_HIRE;
        String error = ApplicationFlow.check(action,
                a.getStatus(), a.getInterviewResult(), a.getHireStatus());
        if (error != null) {
            return error;
        }
        String hireStatus = hired ? ApplicationFlow.HIRED : ApplicationFlow.NOT_HIRED;
        String from = ApplicationFlow.describe(a.getStatus(), a.getInterviewResult(), a.getHireStatus());
        LocalDateTime now = LocalDateTime.now();
        applicationMapper.updateHire(id, hireStatus, operatorId, operatorName, now);
        auditService.record(AuditService.TYPE_APPLICATION, id, hired ? "HIRE" : "NO_HIRE",
                action.getLabel(), from,
                ApplicationFlow.describe(a.getStatus(), a.getInterviewResult(), hireStatus),
                operatorId, operatorName, blankToNull(comment));
        return null;
    }

    // ==================== 内部方法 ====================

    private JobApplication requireForUpdate(Long id) {
        JobApplication application = applicationMapper.findById(id);
        if (application == null) {
            throw new BizException("申请记录不存在或已被删除");
        }
        return application;
    }

    private void publish(Long id, String status, String rejectReason, Long operatorId, String operatorName) {
        applicationMapper.updateReview(id, status, rejectReason, operatorId, operatorName, LocalDateTime.now());
    }

    private static String buildInterviewComment(LocalDateTime time, String place) {
        String text = "面试时间：" + time.toString().replace('T', ' ');
        if (place != null && !place.isBlank()) {
            text += "；地点/方式：" + place.trim();
        }
        return text;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

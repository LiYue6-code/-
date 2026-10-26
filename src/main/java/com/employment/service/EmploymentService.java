package com.employment.service;

import com.employment.common.BizException;
import com.employment.common.PageResult;
import com.employment.entity.AuditLog;
import com.employment.entity.EmploymentInfo;
import com.employment.mapper.EmploymentInfoMapper;
import com.employment.query.EmploymentQuery;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 就业信息业务：学生上报 / 负责人审核（确认、退回补正）/ 学生重新上报。
 */
@Service
public class EmploymentService {

    private final EmploymentInfoMapper employmentInfoMapper;
    private final AuditService auditService;

    public EmploymentService(EmploymentInfoMapper employmentInfoMapper, AuditService auditService) {
        this.employmentInfoMapper = employmentInfoMapper;
        this.auditService = auditService;
    }

    // ==================== 学生端 ====================

    /**
     * 学生上报就业信息。
     *
     * @return null 表示成功，否则返回错误提示
     */
    public String report(EmploymentInfo info) {
        if (info.getCompanyName() == null || info.getCompanyName().isBlank()) {
            return "就业单位不能为空";
        }
        EmploymentInfo active = employmentInfoMapper.findActiveByStudent(info.getStudentId());
        if (active != null) {
            return "你已有一条「" + active.getStatus() + "」的上报记录，无需重复上报";
        }
        employmentInfoMapper.insert(info);
        auditService.record(AuditService.TYPE_EMPLOYMENT, info.getId(), "SUBMIT", "学生提交就业上报",
                null, EmploymentFlow.WAIT, info.getStudentId(), "学生本人", null);
        return null;
    }

    public List<EmploymentInfo> myList(Long studentId) {
        return employmentInfoMapper.findByStudent(studentId);
    }

    /** 学生查看本人某条上报（含归属校验）。 */
    public EmploymentInfo myDetail(Long id, Long studentId) {
        EmploymentInfo info = detail(id);
        if (studentId == null || !studentId.equals(info.getStudentId())) {
            throw new BizException("无权查看他人的就业上报记录");
        }
        return info;
    }

    /**
     * 学生被退回后修改并重新上报。
     *
     * @param proofFile 新的证明材料存储文件名；为 null 表示沿用原文件
     * @param proofName 新证明材料的原始文件名
     */
    public String resubmit(Long id, EmploymentInfo form, Long studentId, String proofFile, String proofName) {
        EmploymentInfo info = myDetail(id, studentId);
        String error = EmploymentFlow.checkResubmit(info.getStatus());
        if (error != null) {
            return error;
        }
        if (form.getCompanyName() == null || form.getCompanyName().isBlank()) {
            return "就业单位不能为空";
        }
        LocalDateTime now = LocalDateTime.now();
        if (proofFile != null) {
            employmentInfoMapper.resubmit(id, form.getCompanyName().trim(), form.getPosition(),
                    form.getSalary(), proofFile, proofName, now);
        } else {
            employmentInfoMapper.resubmitKeepFile(id, form.getCompanyName().trim(), form.getPosition(),
                    form.getSalary(), now);
        }
        auditService.record(AuditService.TYPE_EMPLOYMENT, id, "RESUBMIT", "学生补正后重新上报",
                EmploymentFlow.RETURNED, EmploymentFlow.WAIT, studentId, "学生本人", null);
        return null;
    }

    // ==================== 负责人端 ====================

    public PageResult<EmploymentInfo> search(EmploymentQuery query) {
        long total = employmentInfoMapper.count(query);
        List<EmploymentInfo> items = total == 0 ? List.of() : employmentInfoMapper.search(query);
        return new PageResult<>(items, query.getPage(), query.getSize(), total);
    }

    public EmploymentInfo detail(Long id) {
        EmploymentInfo info = employmentInfoMapper.findDetailById(id);
        if (info == null) {
            throw new BizException("就业上报记录不存在或已被删除");
        }
        return info;
    }

    /** 导出用：按条件取全部记录（不分页）。 */
    public List<EmploymentInfo> export(EmploymentQuery query) {
        EmploymentQuery all = new EmploymentQuery();
        all.setKeyword(query.getKeyword());
        all.setStatus(query.getStatus());
        all.setFrom(query.getFrom());
        all.setTo(query.getTo());
        all.setPage(1);
        all.setSize(10_000);
        return employmentInfoMapper.search(all);
    }

    public List<AuditLog> timeline(Long id) {
        return auditService.timeline(AuditService.TYPE_EMPLOYMENT, id);
    }

    /**
     * 审核就业上报。
     *
     * @param confirmed true=确认通过，false=退回补正
     */
    public String review(Long id, boolean confirmed, String comment,
                         Long operatorId, String operatorName) {
        EmploymentInfo info = employmentInfoMapper.findById(id);
        if (info == null) {
            throw new BizException("就业上报记录不存在或已被删除");
        }
        String error = confirmed
                ? EmploymentFlow.checkConfirm(info.getStatus())
                : EmploymentFlow.checkReturn(info.getStatus());
        if (error != null) {
            return error;
        }
        if (!confirmed && (comment == null || comment.isBlank())) {
            return "退回补正时必须填写需要补正的内容";
        }
        String to = confirmed ? EmploymentFlow.CONFIRMED : EmploymentFlow.RETURNED;
        employmentInfoMapper.updateReview(id, to, comment == null ? null : comment.trim(),
                operatorId, operatorName, LocalDateTime.now());
        auditService.record(AuditService.TYPE_EMPLOYMENT, id,
                confirmed ? "CONFIRM" : "RETURN",
                confirmed ? "确认通过" : "退回补正",
                info.getStatus(), to, operatorId, operatorName,
                comment == null ? null : comment.trim());
        return null;
    }
}

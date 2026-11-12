package com.employment.service;

import com.employment.common.BizException;
import com.employment.common.PageResult;
import com.employment.entity.Job;
import com.employment.entity.JobIntention;
import com.employment.mapper.JobIntentionMapper;
import com.employment.mapper.JobMapper;
import com.employment.query.JobQuery;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 岗位业务。
 *
 * <p>学生端：条件分页检索（仅展示招聘中的岗位）、查看详情、按求职意向推荐。
 * <p>负责人端：全部岗位列表、发布（待审核）、审核（通过/驳回）、手动上下架、有效期自动下架。
 */
@Service
public class JobService {

    private static final int RECOMMEND_LIMIT = 6;

    private final JobMapper jobMapper;
    private final JobIntentionMapper intentionMapper;

    public JobService(JobMapper jobMapper, JobIntentionMapper intentionMapper) {
        this.jobMapper = jobMapper;
        this.intentionMapper = intentionMapper;
    }

    /** 学生端岗位分页检索（仅展示招聘中的岗位）。 */
    public PageResult<Job> search(JobQuery query) {
        markExpired();
        long total = jobMapper.count(query);
        List<Job> items = total == 0 ? List.of() : jobMapper.search(query);
        return new PageResult<>(items, query.getPage(), query.getSize(), total);
    }

    public List<Job> listAll() {
        markExpired();
        return jobMapper.findAll();
    }

    public Job get(Long id) {
        return jobMapper.findById(id);
    }

    /** 学生端岗位详情：岗位不存在时抛出业务异常。 */
    public Job getForStudent(Long id) {
        Job job = jobMapper.findById(id);
        if (job == null) {
            throw new BizException("岗位不存在或已被删除");
        }
        return job;
    }

    /** 发布岗位：默认进入「待审核」且「已截止」，审核通过后才会招聘中。 */
    public void create(Job job) {
        job.setStatus(JobFlow.OFFLINE);
        job.setReviewStatus(JobFlow.REVIEW_WAIT);
        jobMapper.insert(job);
    }

    /** 岗位审核：通过则自动上架（招聘中），驳回则保持下架（已截止）。 */
    public String review(Long id, boolean passed, String comment) {
        Job job = requireForUpdate(id);
        String error = JobFlow.checkReview(job.getReviewStatus());
        if (error != null) {
            return error;
        }
        if (!passed && (comment == null || comment.isBlank())) {
            return "驳回时必须填写审核意见";
        }
        String reviewStatus = passed ? JobFlow.REVIEW_PASSED : JobFlow.REVIEW_REJECTED;
        String status = passed ? JobFlow.ONLINE : JobFlow.OFFLINE;
        jobMapper.review(id, reviewStatus, comment == null ? null : comment.trim(), status);
        return null;
    }

    /** 手动上架。 */
    public String online(Long id) {
        Job job = requireForUpdate(id);
        String error = JobFlow.checkOnline(job.getReviewStatus(), job.getStatus());
        if (error != null) {
            return error;
        }
        if (job.getValidUntil() != null && job.getValidUntil().isBefore(LocalDateTime.now())) {
            return "岗位已过有效期，无法上架";
        }
        jobMapper.updateStatus(id, JobFlow.ONLINE);
        return null;
    }

    /** 手动下架。 */
    public String offline(Long id) {
        Job job = requireForUpdate(id);
        String error = JobFlow.checkOffline(job.getStatus());
        if (error != null) {
            return error;
        }
        jobMapper.updateStatus(id, JobFlow.OFFLINE);
        return null;
    }

    /** 按学生求职意向推荐上架岗位。 */
    public List<Job> recommend(Long studentId) {
        markExpired();
        JobIntention intention = intentionMapper.findByStudent(studentId);
        if (intention == null) {
            return List.of();
        }
        String keyword = firstNonBlank(intention.getPosition(), intention.getIndustry());
        return jobMapper.recommend(keyword, RECOMMEND_LIMIT);
    }

    // ==================== 内部方法 ====================

    /** 过期自动下架：将超过有效期的已上架岗位置为已截止。 */
    private void markExpired() {
        jobMapper.markExpired(LocalDateTime.now());
    }

    private Job requireForUpdate(Long id) {
        Job job = jobMapper.findById(id);
        if (job == null) {
            throw new BizException("岗位不存在或已被删除");
        }
        return job;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
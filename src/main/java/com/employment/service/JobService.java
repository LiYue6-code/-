package com.employment.service;

import com.employment.common.BizException;
import com.employment.common.PageResult;
import com.employment.entity.Job;
import com.employment.mapper.JobMapper;
import com.employment.query.JobQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 岗位业务。
 *
 * <p>学生端：条件分页检索（仅展示招聘中的岗位）、查看详情。
 * <p>负责人端：全部岗位列表、发布/编辑岗位、上架/下架岗位。
 */
@Service
public class JobService {

    /** 岗位状态合法值，与表注释、学生端检索条件、统计口径保持一致。 */
    private static final Set<String> STATUSES = Set.of("招聘中", "已截止");
    private static final int MAX_HEADCOUNT = 9999;

    private final JobMapper jobMapper;

    public JobService(JobMapper jobMapper) {
        this.jobMapper = jobMapper;
    }

    /** 学生端岗位分页检索（仅展示招聘中的岗位）。 */
    public PageResult<Job> search(JobQuery query) {
        long total = jobMapper.count(query);
        List<Job> items = total == 0 ? List.of() : jobMapper.search(query);
        return new PageResult<>(items, query.getPage(), query.getSize(), total);
    }

    public List<Job> listAll() {
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

    /**
     * 负责人端发布岗位。
     *
     * @return 校验通过返回 {@code null}；否则返回中文提示且不写库。
     */
    public String create(Job job) {
        String error = normalize(job);
        if (error != null) {
            return error;
        }
        jobMapper.insert(job);
        return null;
    }

    /**
     * 负责人端编辑岗位：岗位必须存在，字段校验与发布一致。
     *
     * @return 校验通过返回 {@code null}；否则返回中文提示且不写库。
     */
    public String update(Job job) {
        if (job.getId() == null || jobMapper.findById(job.getId()) == null) {
            return "岗位不存在或已被删除";
        }
        String error = normalize(job);
        if (error != null) {
            return error;
        }
        jobMapper.update(job);
        return null;
    }

    /** 下架岗位（状态改为已截止）。 */
    public void offline(Long id) {
        Job job = jobMapper.findById(id);
        if (job != null) {
            job.setStatus("已截止");
            jobMapper.update(job);
        }
    }

    /** 上架岗位（状态改回招聘中）。 */
    public void online(Long id) {
        Job job = jobMapper.findById(id);
        if (job != null) {
            job.setStatus("招聘中");
            jobMapper.update(job);
        }
    }

    /**
     * 字段整理与校验：去空格、补默认值、按表结构长度与取值范围校验。
     * <p>只做与岗位自身有关的校验，招聘单位是否存在由控制器结合 {@link CompanyService} 判断。
     *
     * @return {@code null} 表示通过；否则返回中文错误提示
     */
    private String normalize(Job job) {
        if (job.getCompanyId() == null) {
            return "请选择招聘单位";
        }

        job.setTitle(trim(job.getTitle()));
        if (job.getTitle().isEmpty()) {
            return "岗位名称不能为空";
        }
        if (job.getTitle().length() > 100) {
            return "岗位名称不能超过 100 字";
        }

        job.setSalary(trimToNull(job.getSalary()));
        if (job.getSalary() != null && job.getSalary().length() > 50) {
            return "薪资待遇不能超过 50 字";
        }

        job.setRequirement(trimToNull(job.getRequirement()));
        if (job.getRequirement() != null && job.getRequirement().length() > 500) {
            return "任职要求不能超过 500 字";
        }

        if (job.getDescription() != null) {
            String description = job.getDescription().trim();
            job.setDescription(description.isEmpty() ? null : description);
        }

        if (job.getHeadcount() == null) {
            job.setHeadcount(1);
        }
        if (job.getHeadcount() < 1 || job.getHeadcount() > MAX_HEADCOUNT) {
            return "招聘人数需为 1-" + MAX_HEADCOUNT + " 之间的整数";
        }

        String status = trim(job.getStatus());
        if (status.isEmpty()) {
            status = "招聘中";
        }
        if (!STATUSES.contains(status)) {
            return "岗位状态不合法";
        }
        job.setStatus(status);
        return null;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

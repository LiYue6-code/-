package com.employment.service;

import com.employment.common.BizException;
import com.employment.common.PageResult;
import com.employment.entity.Job;
import com.employment.mapper.JobMapper;
import com.employment.query.JobQuery;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 岗位业务。
 *
 * <p>学生端：条件分页检索（仅展示招聘中的岗位）、查看详情。
 * <p>负责人端：全部岗位列表、新增岗位、下架岗位。
 */
@Service
public class JobService {

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

    public void create(Job job) {
        if (job.getStatus() == null || job.getStatus().isBlank()) {
            job.setStatus("招聘中");
        }
        jobMapper.insert(job);
    }

    /** 下架岗位（状态改为已截止）。 */
    public void offline(Long id) {
        Job job = jobMapper.findById(id);
        if (job != null) {
            job.setStatus("已截止");
            jobMapper.update(job);
        }
    }
}

package com.employment.service;

import com.employment.entity.Job;
import com.employment.mapper.JobMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 岗位业务。
 */
@Service
public class JobService {

    private final JobMapper jobMapper;

    public JobService(JobMapper jobMapper) {
        this.jobMapper = jobMapper;
    }

    /** 学生端岗位检索（仅展示招聘中的岗位）。 */
    public List<Job> search(String keyword) {
        return jobMapper.search(keyword);
    }

    public List<Job> listAll() {
        return jobMapper.findAll();
    }

    public Job get(Long id) {
        return jobMapper.findById(id);
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

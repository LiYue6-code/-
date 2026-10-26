package com.employment.service;

import com.employment.entity.JobApplication;
import com.employment.mapper.JobApplicationMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 岗位申请业务。
 */
@Service
public class ApplicationService {

    private final JobApplicationMapper applicationMapper;

    public ApplicationService(JobApplicationMapper applicationMapper) {
        this.applicationMapper = applicationMapper;
    }

    /**
     * 学生申请岗位。
     *
     * @return 返回 null 表示成功，否则返回错误提示信息。
     */
    public String apply(Long jobId, Long studentId) {
        if (applicationMapper.findByJobAndStudent(jobId, studentId) != null) {
            return "您已申请过该岗位，请勿重复申请";
        }
        applicationMapper.insert(jobId, studentId);
        return null;
    }

    public List<JobApplication> myApplications(Long studentId) {
        return applicationMapper.findByStudent(studentId);
    }

    public List<JobApplication> listAll() {
        return applicationMapper.findAllWithDetail();
    }

    public void review(JobApplication application) {
        applicationMapper.updateReview(application);
    }
}

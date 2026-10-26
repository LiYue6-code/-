package com.employment.service;

import com.employment.entity.EmploymentInfo;
import com.employment.mapper.EmploymentInfoMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 就业信息业务。
 */
@Service
public class EmploymentService {

    private final EmploymentInfoMapper employmentInfoMapper;

    public EmploymentService(EmploymentInfoMapper employmentInfoMapper) {
        this.employmentInfoMapper = employmentInfoMapper;
    }

    public void report(EmploymentInfo info) {
        employmentInfoMapper.insert(info);
    }

    public List<EmploymentInfo> myList(Long studentId) {
        return employmentInfoMapper.findByStudent(studentId);
    }

    public List<EmploymentInfo> listAll() {
        return employmentInfoMapper.findAllWithDetail();
    }

    /** 负责人审核：status 为 已确认 / 已退回。 */
    public void review(Long id, String status, String comment) {
        employmentInfoMapper.updateReview(id, status, comment);
    }
}

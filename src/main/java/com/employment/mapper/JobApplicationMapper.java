package com.employment.mapper;

import com.employment.entity.JobApplication;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 岗位申请表数据访问接口。
 */
public interface JobApplicationMapper {

    @Select("SELECT * FROM job_application WHERE job_id = #{jobId} AND student_id = #{studentId}")
    JobApplication findByJobAndStudent(@Param("jobId") Long jobId, @Param("studentId") Long studentId);

    @Insert("INSERT INTO job_application(job_id, student_id, status) " +
            "VALUES(#{jobId}, #{studentId}, '待审核')")
    int insert(@Param("jobId") Long jobId, @Param("studentId") Long studentId);

    @Select("SELECT a.*, j.title AS job_title, c.name AS company_name FROM job_application a " +
            "LEFT JOIN job j ON a.job_id = j.id " +
            "LEFT JOIN company c ON j.company_id = c.id " +
            "WHERE a.student_id = #{studentId} ORDER BY a.apply_time DESC")
    List<JobApplication> findByStudent(Long studentId);

    @Select("SELECT a.*, j.title AS job_title, c.name AS company_name, " +
            "s.name AS student_name, s.student_no AS student_no FROM job_application a " +
            "LEFT JOIN job j ON a.job_id = j.id " +
            "LEFT JOIN company c ON j.company_id = c.id " +
            "LEFT JOIN student s ON a.student_id = s.id ORDER BY a.apply_time DESC")
    List<JobApplication> findAllWithDetail();

    @Select("SELECT * FROM job_application WHERE id = #{id}")
    JobApplication findById(Long id);

    @Update("UPDATE job_application SET status=#{status}, interview_time=#{interviewTime}, " +
            "interview_result=#{interviewResult}, hire_status=#{hireStatus} WHERE id=#{id}")
    int updateReview(JobApplication application);
}

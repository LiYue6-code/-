package com.employment.mapper;

import com.employment.entity.JobApplication;
import com.employment.query.ApplicationQuery;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 岗位申请表数据访问接口。
 * 复杂条件查询（search / count / findDetailById）见 resources/mapper/JobApplicationMapper.xml。
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
    List<JobApplication> findByStudent(@Param("studentId") Long studentId);

    @Select("SELECT * FROM job_application WHERE id = #{id}")
    JobApplication findById(@Param("id") Long id);

    /** 负责人端条件分页查询（含学生、岗位、单位冗余字段）。 */
    List<JobApplication> search(@Param("q") ApplicationQuery q);

    /** 与 search 相同条件的总条数。 */
    long count(@Param("q") ApplicationQuery q);

    /** 审核详情（含关联字段）。 */
    JobApplication findDetailById(@Param("id") Long id);

    /** 登记初审结果。 */
    @Update("UPDATE job_application SET status = #{status}, reject_reason = #{rejectReason}, " +
            "reviewer_id = #{operatorId}, reviewer_name = #{operatorName}, " +
            "review_time = #{now}, update_time = #{now} WHERE id = #{id}")
    int updateReview(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("rejectReason") String rejectReason,
                     @Param("operatorId") Long operatorId,
                     @Param("operatorName") String operatorName,
                     @Param("now") LocalDateTime now);

    /** 登记面试安排与面试结果。 */
    @Update("UPDATE job_application SET interview_time = #{interviewTime}, " +
            "interview_place = #{interviewPlace}, interview_result = #{interviewResult}, " +
            "reviewer_id = #{operatorId}, reviewer_name = #{operatorName}, " +
            "review_time = #{now}, update_time = #{now} WHERE id = #{id}")
    int updateInterview(@Param("id") Long id,
                        @Param("interviewTime") LocalDateTime interviewTime,
                        @Param("interviewPlace") String interviewPlace,
                        @Param("interviewResult") String interviewResult,
                        @Param("operatorId") Long operatorId,
                        @Param("operatorName") String operatorName,
                        @Param("now") LocalDateTime now);

    /** 登记录用结果。 */
    @Update("UPDATE job_application SET hire_status = #{hireStatus}, " +
            "reviewer_id = #{operatorId}, reviewer_name = #{operatorName}, " +
            "review_time = #{now}, update_time = #{now} WHERE id = #{id}")
    int updateHire(@Param("id") Long id,
                   @Param("hireStatus") String hireStatus,
                   @Param("operatorId") Long operatorId,
                   @Param("operatorName") String operatorName,
                   @Param("now") LocalDateTime now);
}

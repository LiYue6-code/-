package com.employment.mapper;

import com.employment.entity.JobIntention;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

/**
 * 求职意向表数据访问接口。
 */
public interface JobIntentionMapper {

    @Select("SELECT * FROM job_intention WHERE student_id = #{studentId}")
    JobIntention findByStudent(Long studentId);

    /** 保存意向：student_id 唯一，存在则更新，不存在则插入。 */
    @Insert("INSERT INTO job_intention(student_id, industry, position, city, salary) " +
            "VALUES(#{studentId}, #{industry}, #{position}, #{city}, #{salary}) " +
            "ON DUPLICATE KEY UPDATE industry=#{industry}, position=#{position}, city=#{city}, salary=#{salary}")
    int save(JobIntention intention);
}
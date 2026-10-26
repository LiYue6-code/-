package com.employment.mapper;

import com.employment.entity.Job;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 岗位表数据访问接口。
 */
public interface JobMapper {

    @Select("SELECT j.*, c.name AS company_name FROM job j " +
            "LEFT JOIN company c ON j.company_id = c.id WHERE j.id = #{id}")
    Job findById(Long id);

    @Select("SELECT j.*, c.name AS company_name FROM job j " +
            "LEFT JOIN company c ON j.company_id = c.id ORDER BY j.publish_time DESC")
    List<Job> findAll();

    @Select("<script>" +
            "SELECT j.*, c.name AS company_name FROM job j " +
            "LEFT JOIN company c ON j.company_id = c.id WHERE j.status = '招聘中' " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (j.title LIKE CONCAT('%', #{keyword}, '%') OR c.name LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if> ORDER BY j.publish_time DESC" +
            "</script>")
    List<Job> search(@Param("keyword") String keyword);

    @Insert("INSERT INTO job(company_id, title, description, requirement, headcount, salary, status) " +
            "VALUES(#{companyId}, #{title}, #{description}, #{requirement}, #{headcount}, #{salary}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Job job);

    @Update("UPDATE job SET title=#{title}, description=#{description}, requirement=#{requirement}, " +
            "headcount=#{headcount}, salary=#{salary}, status=#{status} WHERE id=#{id}")
    int update(Job job);
}

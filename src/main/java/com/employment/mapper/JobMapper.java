package com.employment.mapper;

import com.employment.entity.Job;
import com.employment.query.AdminJobQuery;
import com.employment.query.JobQuery;
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

    /**
     * 学生端条件分页查询（仅招聘中的岗位）。
     * SQL 见 resources/mapper/JobMapper.xml。
     */
    List<Job> search(@Param("q") JobQuery q);

    /** 与 search 相同条件的总条数。 */
    long count(@Param("q") JobQuery q);

    @Insert("INSERT INTO job(company_id, title, description, requirement, headcount, salary, status) " +
            "VALUES(#{companyId}, #{title}, #{description}, #{requirement}, #{headcount}, #{salary}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Job job);

    @Update("UPDATE job SET company_id=#{companyId}, title=#{title}, description=#{description}, " +
            "requirement=#{requirement}, headcount=#{headcount}, salary=#{salary}, " +
            "status=#{status} WHERE id=#{id}")
    int update(Job job);

    /** 某招聘单位下的岗位数量（删除单位前的保护性检查）。 */
    @Select("SELECT COUNT(*) FROM job WHERE company_id = #{companyId}")
    int countByCompany(@Param("companyId") Long companyId);

    /**
     * 负责人端条件分页查询（含已截止岗位）。
     * SQL 见 resources/mapper/JobMapper.xml，与学生端 {@link #search} 分开，互不影响。
     */
    List<Job> adminSearch(@Param("q") AdminJobQuery q);

    /** 与 adminSearch 相同条件的总条数。 */
    long adminCount(@Param("q") AdminJobQuery q);
}

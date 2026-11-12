package com.employment.mapper;

import com.employment.entity.Job;
import com.employment.query.JobQuery;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
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

    /** 按关键词推荐上架岗位（用于求职意向推荐）。 */
    @Select("<script>" +
            "SELECT j.*, c.name AS company_name FROM job j " +
            "LEFT JOIN company c ON j.company_id = c.id " +
            "WHERE j.status = '招聘中' " +
            "<if test=\"kw != null and kw != ''\">" +
            "AND (j.title LIKE CONCAT('%', #{kw}, '%') OR c.type LIKE CONCAT('%', #{kw}, '%'))" +
            "</if> ORDER BY j.publish_time DESC LIMIT #{limit}" +
            "</script>")
    List<Job> recommend(@Param("kw") String kw, @Param("limit") int limit);

    @Insert("INSERT INTO job(company_id, title, description, requirement, headcount, salary, status, review_status, valid_until) " +
            "VALUES(#{companyId}, #{title}, #{description}, #{requirement}, #{headcount}, #{salary}, #{status}, #{reviewStatus}, #{validUntil})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Job job);

    @Update("UPDATE job SET title=#{title}, description=#{description}, requirement=#{requirement}, " +
            "headcount=#{headcount}, salary=#{salary}, status=#{status}, review_status=#{reviewStatus}, " +
            "valid_until=#{validUntil} WHERE id=#{id}")
    int update(Job job);

    /** 岗位审核：设置审核状态、审核意见，并联动上架状态（通过→招聘中，驳回→已截止）。 */
    @Update("UPDATE job SET review_status=#{reviewStatus}, review_comment=#{reviewComment}, status=#{status} WHERE id=#{id}")
    int review(@Param("id") Long id, @Param("reviewStatus") String reviewStatus,
               @Param("reviewComment") String reviewComment, @Param("status") String status);

    /** 手动上下架：仅修改上架状态。 */
    @Update("UPDATE job SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /** 过期自动下架：将已上架且超过有效期的岗位置为已截止。 */
    @Update("UPDATE job SET status = '已截止' WHERE status = '招聘中' AND valid_until IS NOT NULL AND valid_until < #{now}")
    int markExpired(LocalDateTime now);
}
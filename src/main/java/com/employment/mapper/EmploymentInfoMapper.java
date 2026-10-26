package com.employment.mapper;

import com.employment.entity.EmploymentInfo;
import com.employment.query.EmploymentQuery;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 就业信息表数据访问接口。
 * 复杂条件查询（search / count / findDetailById）见 resources/mapper/EmploymentInfoMapper.xml。
 */
public interface EmploymentInfoMapper {

    @Insert("INSERT INTO employment_info(student_id, company_name, position, salary, proof_file, proof_name, status) " +
            "VALUES(#{studentId}, #{companyName}, #{position}, #{salary}, #{proofFile}, #{proofName}, '待审核')")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(EmploymentInfo info);

    @Select("SELECT * FROM employment_info WHERE student_id = #{studentId} ORDER BY submit_time DESC")
    List<EmploymentInfo> findByStudent(@Param("studentId") Long studentId);

    @Select("SELECT * FROM employment_info WHERE id = #{id}")
    EmploymentInfo findById(@Param("id") Long id);

    /** 学生当前处于“待审核/已确认”的上报（用于防止重复上报）。 */
    @Select("SELECT * FROM employment_info WHERE student_id = #{studentId} " +
            "AND status IN ('待审核', '已确认') ORDER BY submit_time DESC LIMIT 1")
    EmploymentInfo findActiveByStudent(@Param("studentId") Long studentId);

    /** 负责人端条件分页查询。 */
    List<EmploymentInfo> search(@Param("q") EmploymentQuery q);

    /** 与 search 相同条件的总条数。 */
    long count(@Param("q") EmploymentQuery q);

    /** 审核详情（含学生信息）。 */
    EmploymentInfo findDetailById(@Param("id") Long id);

    /** 登记审核结果。 */
    @Update("UPDATE employment_info SET status = #{status}, review_comment = #{comment}, " +
            "reviewer_id = #{operatorId}, reviewer_name = #{operatorName}, " +
            "review_time = #{now}, update_time = #{now} WHERE id = #{id}")
    int updateReview(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("comment") String comment,
                     @Param("operatorId") Long operatorId,
                     @Param("operatorName") String operatorName,
                     @Param("now") LocalDateTime now);

    /** 学生修改后重新上报（状态回到待审核，清空上次审核意见，重报次数 +1）。 */
    @Update("UPDATE employment_info SET company_name = #{companyName}, position = #{position}, " +
            "salary = #{salary}, proof_file = #{proofFile}, proof_name = #{proofName}, " +
            "status = '待审核', review_comment = NULL, " +
            "resubmit_count = resubmit_count + 1, update_time = #{now} WHERE id = #{id}")
    int resubmit(@Param("id") Long id,
                 @Param("companyName") String companyName,
                 @Param("position") String position,
                 @Param("salary") String salary,
                 @Param("proofFile") String proofFile,
                 @Param("proofName") String proofName,
                 @Param("now") LocalDateTime now);

    /** 仅更新证明材料（重新上报时未更换文件）。 */
    @Update("UPDATE employment_info SET company_name = #{companyName}, position = #{position}, " +
            "salary = #{salary}, status = '待审核', review_comment = NULL, " +
            "resubmit_count = resubmit_count + 1, update_time = #{now} WHERE id = #{id}")
    int resubmitKeepFile(@Param("id") Long id,
                         @Param("companyName") String companyName,
                         @Param("position") String position,
                         @Param("salary") String salary,
                         @Param("now") LocalDateTime now);
}

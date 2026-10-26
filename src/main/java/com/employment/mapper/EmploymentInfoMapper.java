package com.employment.mapper;

import com.employment.entity.EmploymentInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 就业信息表数据访问接口。
 */
public interface EmploymentInfoMapper {

    @Insert("INSERT INTO employment_info(student_id, company_name, position, salary, proof_file, status) " +
            "VALUES(#{studentId}, #{companyName}, #{position}, #{salary}, #{proofFile}, '待审核')")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(EmploymentInfo info);

    @Select("SELECT * FROM employment_info WHERE student_id = #{studentId} ORDER BY submit_time DESC")
    List<EmploymentInfo> findByStudent(Long studentId);

    @Select("SELECT e.*, s.name AS student_name, s.student_no AS student_no FROM employment_info e " +
            "LEFT JOIN student s ON e.student_id = s.id ORDER BY e.submit_time DESC")
    List<EmploymentInfo> findAllWithDetail();

    @Select("SELECT * FROM employment_info WHERE id = #{id}")
    EmploymentInfo findById(Long id);

    @Update("UPDATE employment_info SET status=#{status}, review_comment=#{reviewComment} WHERE id=#{id}")
    int updateReview(@Param("id") Long id, @Param("status") String status,
                     @Param("reviewComment") String reviewComment);
}

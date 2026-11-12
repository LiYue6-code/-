package com.employment.mapper;

import com.employment.entity.Company;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 招聘单位表数据访问接口。
 */
public interface CompanyMapper {

    @Select("SELECT * FROM company ORDER BY id DESC")
    List<Company> findAll();

    /** 仅返回资质审核通过的单位（用于岗位发布下拉选择）。 */
    @Select("SELECT * FROM company WHERE review_status = '通过' ORDER BY id DESC")
    List<Company> findAllApproved();

    @Select("SELECT * FROM company WHERE id = #{id}")
    Company findById(Long id);

    @Insert("INSERT INTO company(name, type, contact, phone, address, description, qualification_file, qualification_name) " +
            "VALUES(#{name}, #{type}, #{contact}, #{phone}, #{address}, #{description}, #{qualificationFile}, #{qualificationName})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Company company);

    @Update("UPDATE company SET name=#{name}, type=#{type}, contact=#{contact}, phone=#{phone}, " +
            "address=#{address}, description=#{description}, qualification_file=#{qualificationFile}, " +
            "qualification_name=#{qualificationName} WHERE id=#{id}")
    int update(Company company);

    /** 资质审核：设置审核状态与审核意见。 */
    @Update("UPDATE company SET review_status=#{reviewStatus}, review_comment=#{reviewComment} WHERE id=#{id}")
    int review(@Param("id") Long id, @Param("reviewStatus") String reviewStatus,
               @Param("reviewComment") String reviewComment);

    /** 信用状态标记：正常 / 异常。 */
    @Update("UPDATE company SET credit_status = #{creditStatus} WHERE id = #{id}")
    int updateCreditStatus(@Param("id") Long id, @Param("creditStatus") String creditStatus);

    @Delete("DELETE FROM company WHERE id = #{id}")
    int deleteById(Long id);
}
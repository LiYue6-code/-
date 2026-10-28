package com.employment.mapper;

import com.employment.entity.Company;
import com.employment.query.AdminCompanyQuery;
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

    @Select("SELECT * FROM company WHERE id = #{id}")
    Company findById(Long id);

    @Insert("INSERT INTO company(name, type, contact, phone, address, description) " +
            "VALUES(#{name}, #{type}, #{contact}, #{phone}, #{address}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Company company);

    @Update("UPDATE company SET name=#{name}, type=#{type}, contact=#{contact}, phone=#{phone}, " +
            "address=#{address}, description=#{description} WHERE id=#{id}")
    int update(Company company);

    @Delete("DELETE FROM company WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 负责人端条件分页查询（带岗位数）。
     * SQL 见 resources/mapper/CompanyMapper.xml。
     */
    List<Company> search(@Param("q") AdminCompanyQuery q);

    /** 与 search 相同条件的总条数。 */
    long count(@Param("q") AdminCompanyQuery q);
}

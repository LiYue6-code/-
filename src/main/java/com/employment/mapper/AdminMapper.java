package com.employment.mapper;

import com.employment.entity.Admin;
import org.apache.ibatis.annotations.Select;

/**
 * 学校负责人表数据访问接口。
 */
public interface AdminMapper {

    @Select("SELECT * FROM admin WHERE username = #{username}")
    Admin findByUsername(String username);
}

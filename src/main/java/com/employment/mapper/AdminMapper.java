package com.employment.mapper;

import com.employment.entity.Admin;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学校负责人表数据访问接口。
 */
public interface AdminMapper {

    @Select("SELECT * FROM admin WHERE username = #{username}")
    Admin findByUsername(String username);

    @Select("SELECT * FROM admin")
    List<Admin> findAll();

    /** 密码迁移：仅更新密码字段。 */
    @Update("UPDATE admin SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    /** 登录失败：失败次数 +1。 */
    @Update("UPDATE admin SET failed_count = failed_count + 1 WHERE id = #{id}")
    int incrementFailedCount(Long id);

    /** 达到失败上限：锁定账号并清零失败次数。 */
    @Update("UPDATE admin SET failed_count = 0, locked_until = #{lockedUntil} WHERE id = #{id}")
    int lock(@Param("id") Long id, @Param("lockedUntil") LocalDateTime lockedUntil);

    /** 登录成功：清零失败次数并解除锁定。 */
    @Update("UPDATE admin SET failed_count = 0, locked_until = NULL WHERE id = #{id}")
    int resetLoginFail(Long id);
}

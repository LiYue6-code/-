package com.employment.mapper;

import com.employment.entity.Student;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生表数据访问接口。
 */
public interface StudentMapper {

    @Select("SELECT * FROM student WHERE student_no = #{studentNo}")
    Student findByStudentNo(String studentNo);

    @Select("SELECT * FROM student WHERE id = #{id}")
    Student findById(Long id);

    @Select("SELECT * FROM student")
    List<Student> findAll();

    @Insert("INSERT INTO student(student_no, password, name, major, phone, email) " +
            "VALUES(#{studentNo}, #{password}, #{name}, #{major}, #{phone}, #{email})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Student student);

    /** 学生自助：仅改个人资料，不动学号/密码/状态。 */
    @Update("UPDATE student SET name=#{name}, major=#{major}, phone=#{phone}, email=#{email} " +
            "WHERE id=#{id}")
    int updateProfile(Student student);

    /** 管理员：改个人资料与状态。 */
    @Update("UPDATE student SET name=#{name}, major=#{major}, phone=#{phone}, email=#{email}, status=#{status} " +
            "WHERE id=#{id}")
    int updateByAdmin(Student student);

    @Update("UPDATE student SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE student SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Delete("DELETE FROM student WHERE id = #{id}")
    int deleteById(Long id);

    /** 分页搜索：按学号/姓名/专业模糊匹配，可选状态过滤。 */
    @Select("<script>" +
            "SELECT * FROM student " +
            "<where>" +
            "  <if test=\"keyword != null\">" +
            "    AND (student_no LIKE CONCAT('%', #{keyword}, '%') " +
            "         OR name LIKE CONCAT('%', #{keyword}, '%') " +
            "         OR major LIKE CONCAT('%', #{keyword}, '%'))" +
            "  </if>" +
            "  <if test=\"status != null\">AND status = #{status}</if>" +
            "</where>" +
            "ORDER BY student_no LIMIT #{limit} OFFSET #{offset}" +
            "</script>")
    List<Student> search(@Param("keyword") String keyword, @Param("status") String status,
                         @Param("offset") int offset, @Param("limit") int limit);

    @Select("<script>" +
            "SELECT COUNT(*) FROM student " +
            "<where>" +
            "  <if test=\"keyword != null\">" +
            "    AND (student_no LIKE CONCAT('%', #{keyword}, '%') " +
            "         OR name LIKE CONCAT('%', #{keyword}, '%') " +
            "         OR major LIKE CONCAT('%', #{keyword}, '%'))" +
            "  </if>" +
            "  <if test=\"status != null\">AND status = #{status}</if>" +
            "</where>" +
            "</script>")
    long count(@Param("keyword") String keyword, @Param("status") String status);

    /** 登录失败：失败次数 +1。 */
    @Update("UPDATE student SET failed_count = failed_count + 1 WHERE id = #{id}")
    int incrementFailedCount(Long id);

    /** 达到失败上限：锁定账号并清零失败次数。 */
    @Update("UPDATE student SET failed_count = 0, locked_until = #{lockedUntil} WHERE id = #{id}")
    int lock(@Param("id") Long id, @Param("lockedUntil") LocalDateTime lockedUntil);

    /** 登录成功：清零失败次数并解除锁定。 */
    @Update("UPDATE student SET failed_count = 0, locked_until = NULL WHERE id = #{id}")
    int resetLoginFail(Long id);
}

package com.employment.mapper;

import com.employment.entity.Student;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 学生表数据访问接口。
 */
public interface StudentMapper {

    @Select("SELECT * FROM student WHERE student_no = #{studentNo}")
    Student findByStudentNo(String studentNo);

    @Select("SELECT * FROM student WHERE id = #{id}")
    Student findById(Long id);

    @Insert("INSERT INTO student(student_no, password, name, major, phone, email) " +
            "VALUES(#{studentNo}, #{password}, #{name}, #{major}, #{phone}, #{email})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Student student);

    @Update("UPDATE student SET name=#{name}, major=#{major}, phone=#{phone}, email=#{email} " +
            "WHERE id=#{id}")
    int update(Student student);
}

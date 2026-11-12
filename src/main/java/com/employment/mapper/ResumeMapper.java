package com.employment.mapper;

import com.employment.entity.Resume;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 学生简历表数据访问接口。
 */
public interface ResumeMapper {

    @Select("SELECT * FROM resume WHERE student_id = #{studentId} ORDER BY default_flag DESC, update_time DESC")
    List<Resume> findByStudent(Long studentId);

    @Select("SELECT * FROM resume WHERE id = #{id}")
    Resume findById(Long id);

    @Select("SELECT * FROM resume WHERE student_id = #{studentId} AND default_flag = 1 LIMIT 1")
    Resume findDefault(Long studentId);

    @Insert("INSERT INTO resume(student_id, title, education, school, major, graduate_year, " +
            "experience, skills, self_eval, default_flag) " +
            "VALUES(#{studentId}, #{title}, #{education}, #{school}, #{major}, #{graduateYear}, " +
            "#{experience}, #{skills}, #{selfEval}, #{defaultFlag})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Resume resume);

    @Update("UPDATE resume SET title=#{title}, education=#{education}, school=#{school}, major=#{major}, " +
            "graduate_year=#{graduateYear}, experience=#{experience}, skills=#{skills}, self_eval=#{selfEval} " +
            "WHERE id=#{id}")
    int update(Resume resume);

    @Delete("DELETE FROM resume WHERE id = #{id}")
    int deleteById(Long id);

    /** 将某学生的所有简历取消默认标记。 */
    @Update("UPDATE resume SET default_flag = 0 WHERE student_id = #{studentId}")
    int clearDefault(Long studentId);

    /** 设置某简历为默认简历。 */
    @Update("UPDATE resume SET default_flag = 1 WHERE id = #{id} AND student_id = #{studentId}")
    int setDefault(@Param("id") Long id, @Param("studentId") Long studentId);
}
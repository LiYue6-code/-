package com.employment.service;

import com.employment.entity.Student;
import com.employment.mapper.StudentMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 学生信息维护业务：管理员端的增删改查/重置密码，学生端的个人资料与修改密码。
 */
@Service
public class StudentService {

    private static final int PAGE_SIZE = 10;

    private final StudentMapper studentMapper;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentMapper studentMapper, PasswordEncoder passwordEncoder) {
        this.studentMapper = studentMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /** 分页搜索学生列表。 */
    public List<Student> search(String keyword, String status, int page) {
        int offset = (page - 1) * PAGE_SIZE;
        return studentMapper.search(normalize(keyword), normalize(status), offset, PAGE_SIZE);
    }

    /** 总页数。 */
    public int totalPages(String keyword, String status) {
        long total = studentMapper.count(normalize(keyword), normalize(status));
        return (int) Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    public Student get(Long id) {
        return studentMapper.findById(id);
    }

    public boolean existsByStudentNo(String studentNo) {
        return studentMapper.findByStudentNo(studentNo) != null;
    }

    /** 管理员新增学生：密码 BCrypt 加密后入库（状态由数据库默认「正常」）。 */
    public void create(Student student) {
        student.setPassword(passwordEncoder.encode(student.getPassword()));
        studentMapper.insert(student);
    }

    /** 管理员编辑：改资料与状态。 */
    public void updateByAdmin(Student student) {
        studentMapper.updateByAdmin(student);
    }

    /** 学生自助：只改自己的资料。 */
    public void updateProfile(Student student) {
        studentMapper.updateProfile(student);
    }

    public void delete(Long id) {
        studentMapper.deleteById(id);
    }

    public void resetPassword(Long id, String newPassword) {
        studentMapper.updatePassword(id, passwordEncoder.encode(newPassword));
    }

    public void updateStatus(Long id, String status) {
        studentMapper.updateStatus(id, status);
    }

    /** 学生自助改密码：校验当前密码，通过后加密新密码。 */
    public boolean changePassword(Long id, String oldPassword, String newPassword) {
        Student student = studentMapper.findById(id);
        if (student == null) {
            return false;
        }
        if (!passwordEncoder.matches(oldPassword, student.getPassword())) {
            return false;
        }
        studentMapper.updatePassword(id, passwordEncoder.encode(newPassword));
        return true;
    }

    private String normalize(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}

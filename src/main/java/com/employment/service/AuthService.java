package com.employment.service;

import com.employment.entity.Admin;
import com.employment.entity.Student;
import com.employment.mapper.AdminMapper;
import com.employment.mapper.StudentMapper;
import org.springframework.stereotype.Service;

/**
 * 认证相关业务：登录、注册。
 */
@Service
public class AuthService {

    private final StudentMapper studentMapper;
    private final AdminMapper adminMapper;

    public AuthService(StudentMapper studentMapper, AdminMapper adminMapper) {
        this.studentMapper = studentMapper;
        this.adminMapper = adminMapper;
    }

    /** 学生登录，成功返回学生实体，失败返回 null。 */
    public Student studentLogin(String studentNo, String password) {
        Student student = studentMapper.findByStudentNo(studentNo);
        if (student != null && student.getPassword().equals(password)) {
            return student;
        }
        return null;
    }

    /** 负责人登录，成功返回负责人实体，失败返回 null。 */
    public Admin adminLogin(String username, String password) {
        Admin admin = adminMapper.findByUsername(username);
        if (admin != null && admin.getPassword().equals(password)) {
            return admin;
        }
        return null;
    }

    /** 学生注册，学号已存在返回 false。 */
    public boolean registerStudent(Student student) {
        if (studentMapper.findByStudentNo(student.getStudentNo()) != null) {
            return false;
        }
        studentMapper.insert(student);
        return true;
    }
}

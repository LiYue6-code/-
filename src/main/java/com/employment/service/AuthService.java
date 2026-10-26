package com.employment.service;

import com.employment.entity.Admin;
import com.employment.entity.Student;
import com.employment.mapper.AdminMapper;
import com.employment.mapper.StudentMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 认证相关业务：登录、注册。
 */
@Service
public class AuthService {

    /** 最大允许连续失败次数，超过则锁定。 */
    private static final int MAX_FAILED = 5;
    /** 锁定时长（分钟）。 */
    private static final long LOCK_MINUTES = 15;

    private final StudentMapper studentMapper;
    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthService(StudentMapper studentMapper, AdminMapper adminMapper,
                       PasswordEncoder passwordEncoder) {
        this.studentMapper = studentMapper;
        this.adminMapper = adminMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /** 按角色登录，返回结果（成功携带主体，失败携带提示）。 */
    public LoginResult login(String username, String password, String role) {
        if ("admin".equals(role)) {
            return adminLogin(username, password);
        }
        return studentLogin(username, password);
    }

    private LoginResult adminLogin(String username, String password) {
        Admin admin = adminMapper.findByUsername(username);
        if (admin == null) {
            return LoginResult.failure("账号或密码错误");
        }
        if (isLocked(admin.getLockedUntil())) {
            return LoginResult.failure("账号已锁定，请 " + remainingMinutes(admin.getLockedUntil()) + " 分钟后重试");
        }
        if (passwordEncoder.matches(password, admin.getPassword())) {
            adminMapper.resetLoginFail(admin.getId());
            return LoginResult.success(admin);
        }
        recordFailureAdmin(admin);
        return LoginResult.failure("账号或密码错误");
    }

    private LoginResult studentLogin(String studentNo, String password) {
        Student student = studentMapper.findByStudentNo(studentNo);
        if (student == null) {
            return LoginResult.failure("账号或密码错误");
        }
        if ("禁用".equals(student.getStatus())) {
            return LoginResult.failure("账号已被禁用，请联系管理员");
        }
        if (isLocked(student.getLockedUntil())) {
            return LoginResult.failure("账号已锁定，请 " + remainingMinutes(student.getLockedUntil()) + " 分钟后重试");
        }
        if (passwordEncoder.matches(password, student.getPassword())) {
            studentMapper.resetLoginFail(student.getId());
            return LoginResult.success(student);
        }
        recordFailureStudent(student);
        return LoginResult.failure("账号或密码错误");
    }

    private boolean isLocked(LocalDateTime lockedUntil) {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    private long remainingMinutes(LocalDateTime lockedUntil) {
        long minutes = Duration.between(LocalDateTime.now(), lockedUntil).toMinutes();
        return Math.max(minutes, 1);
    }

    private void recordFailureStudent(Student student) {
        if (student.getFailedCount() + 1 >= MAX_FAILED) {
            studentMapper.lock(student.getId(), LocalDateTime.now().plusMinutes(LOCK_MINUTES));
        } else {
            studentMapper.incrementFailedCount(student.getId());
        }
    }

    private void recordFailureAdmin(Admin admin) {
        if (admin.getFailedCount() + 1 >= MAX_FAILED) {
            adminMapper.lock(admin.getId(), LocalDateTime.now().plusMinutes(LOCK_MINUTES));
        } else {
            adminMapper.incrementFailedCount(admin.getId());
        }
    }

    /** 学生注册，学号已存在返回 false；密码使用 BCrypt 加密后入库。 */
    public boolean registerStudent(Student student) {
        if (studentMapper.findByStudentNo(student.getStudentNo()) != null) {
            return false;
        }
        student.setPassword(passwordEncoder.encode(student.getPassword()));
        studentMapper.insert(student);
        return true;
    }
}

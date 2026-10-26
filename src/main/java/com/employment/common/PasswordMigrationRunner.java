package com.employment.common;

import com.employment.entity.Admin;
import com.employment.entity.Student;
import com.employment.mapper.AdminMapper;
import com.employment.mapper.StudentMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动时把历史明文密码升级为 BCrypt 密文。
 * 幂等：只处理非 BCrypt 前缀（$2a/$2b/$2y）的密码，已加密的密码不会重复处理。
 */
@Component
public class PasswordMigrationRunner implements CommandLineRunner {

    private final StudentMapper studentMapper;
    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;

    public PasswordMigrationRunner(StudentMapper studentMapper, AdminMapper adminMapper,
                                   PasswordEncoder passwordEncoder) {
        this.studentMapper = studentMapper;
        this.adminMapper = adminMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        List<Student> students = studentMapper.findAll();
        for (Student s : students) {
            if (isPlaintext(s.getPassword())) {
                studentMapper.updatePassword(s.getId(), passwordEncoder.encode(s.getPassword()));
            }
        }
        List<Admin> admins = adminMapper.findAll();
        for (Admin a : admins) {
            if (isPlaintext(a.getPassword())) {
                adminMapper.updatePassword(a.getId(), passwordEncoder.encode(a.getPassword()));
            }
        }
    }

    private boolean isPlaintext(String password) {
        return password != null && !password.startsWith("$2");
    }
}

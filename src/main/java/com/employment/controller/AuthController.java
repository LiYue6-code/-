package com.employment.controller;

import com.employment.entity.Admin;
import com.employment.entity.Student;
import com.employment.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 认证相关控制器：登录、注册、退出。
 */
@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        @RequestParam String role,
                        HttpSession session,
                        Model model) {
        if ("admin".equals(role)) {
            Admin admin = authService.adminLogin(username, password);
            if (admin != null) {
                session.setAttribute("userType", "admin");
                session.setAttribute("userId", admin.getId());
                session.setAttribute("userName", admin.getName());
                return "redirect:/home";
            }
        } else {
            Student student = authService.studentLogin(username, password);
            if (student != null) {
                session.setAttribute("userType", "student");
                session.setAttribute("userId", student.getId());
                session.setAttribute("userName", student.getName());
                return "redirect:/home";
            }
        }
        model.addAttribute("error", "账号或密码错误");
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(Student student, Model model) {
        if (student.getStudentNo() == null || student.getStudentNo().isBlank()
                || student.getPassword() == null || student.getPassword().isBlank()) {
            model.addAttribute("error", "学号和密码不能为空");
            return "register";
        }
        if (!authService.registerStudent(student)) {
            model.addAttribute("error", "该学号已被注册");
            return "register";
        }
        return "redirect:/login?registered";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}

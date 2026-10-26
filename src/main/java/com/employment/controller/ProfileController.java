package com.employment.controller;

import com.employment.entity.Student;
import com.employment.service.StudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 学生个人中心：查看/修改本人资料、修改密码。
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final StudentService studentService;

    public ProfileController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public String view(HttpSession session, Model model) {
        Long id = (Long) session.getAttribute("userId");
        model.addAttribute("student", studentService.get(id));
        return "profile";
    }

    /** 更新本人资料，学生 ID 仅从会话获取，不接受前端传入。 */
    @PostMapping
    public String update(Student student, HttpSession session, Model model) {
        Long id = (Long) session.getAttribute("userId");
        student.setId(id);
        studentService.updateProfile(student);
        session.setAttribute("userName", student.getName());
        model.addAttribute("student", studentService.get(id));
        model.addAttribute("success", "保存成功");
        return "profile";
    }

    @GetMapping("/password")
    public String passwordForm() {
        return "change-password";
    }

    @PostMapping("/password")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpSession session,
                                 Model model) {
        Long id = (Long) session.getAttribute("userId");
        if (newPassword == null || newPassword.length() < 6) {
            model.addAttribute("error", "新密码至少 6 位");
            return "change-password";
        }
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "两次输入的新密码不一致");
            return "change-password";
        }
        if (!studentService.changePassword(id, oldPassword, newPassword)) {
            model.addAttribute("error", "当前密码错误");
            return "change-password";
        }
        // 修改成功后强制重新登录
        session.invalidate();
        return "redirect:/login?passwordChanged";
    }
}

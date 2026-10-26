package com.employment.controller;

import com.employment.entity.Student;
import com.employment.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 学生信息维护（负责人端）：列表搜索、新增、编辑、删除、重置密码、禁用/启用。
 */
@Controller
@RequestMapping("/admin/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String status,
                       @RequestParam(defaultValue = "1") int page,
                       Model model) {
        model.addAttribute("students", studentService.search(keyword, status, page));
        model.addAttribute("totalPages", studentService.totalPages(keyword, status));
        model.addAttribute("page", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        return "admin/student-list";
    }

    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("student", new Student());
        return "admin/student-form";
    }

    @PostMapping
    public String create(Student student, Model model) {
        if (isBlank(student.getStudentNo()) || isBlank(student.getPassword()) || isBlank(student.getName())) {
            model.addAttribute("error", "学号、姓名、密码不能为空");
            model.addAttribute("student", student);
            return "admin/student-form";
        }
        if (studentService.existsByStudentNo(student.getStudentNo())) {
            model.addAttribute("error", "该学号已存在");
            model.addAttribute("student", student);
            return "admin/student-form";
        }
        studentService.create(student);
        return "redirect:/admin/students";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("student", studentService.get(id));
        return "admin/student-form";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id, Student student, @RequestParam String status) {
        student.setId(id);
        student.setStatus(status);
        studentService.updateByAdmin(student);
        return "redirect:/admin/students";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        studentService.delete(id);
        return "redirect:/admin/students";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id, @RequestParam String status) {
        studentService.updateStatus(id, status);
        return "redirect:/admin/students";
    }

    @GetMapping("/{id}/reset-password")
    public String resetPasswordForm(@PathVariable Long id, Model model) {
        model.addAttribute("student", studentService.get(id));
        return "admin/student-reset-password";
    }

    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable Long id, @RequestParam String password, Model model) {
        if (isBlank(password)) {
            model.addAttribute("student", studentService.get(id));
            model.addAttribute("error", "密码不能为空");
            return "admin/student-reset-password";
        }
        studentService.resetPassword(id, password);
        return "redirect:/admin/students?reset=1";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

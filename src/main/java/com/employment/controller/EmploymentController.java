package com.employment.controller;

import com.employment.entity.EmploymentInfo;
import com.employment.service.EmploymentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 就业信息控制器：学生上报，负责人审核。
 */
@Controller
public class EmploymentController {

    private final EmploymentService employmentService;

    public EmploymentController(EmploymentService employmentService) {
        this.employmentService = employmentService;
    }

    @GetMapping("/my/employment")
    public String my(HttpSession session, Model model) {
        Long studentId = (Long) session.getAttribute("userId");
        model.addAttribute("list", employmentService.myList(studentId));
        model.addAttribute("info", new EmploymentInfo());
        return "my-employment";
    }

    /** 学生上报就业信息及证明材料。 */
    @PostMapping("/my/employment")
    public String report(EmploymentInfo info,
                         @RequestParam(required = false) MultipartFile proofFile,
                         HttpSession session) {
        Long studentId = (Long) session.getAttribute("userId");
        info.setStudentId(studentId);
        info.setProofFile(saveFile(proofFile));
        employmentService.report(info);
        return "redirect:/my/employment";
    }

    @GetMapping("/admin/employment")
    public String adminList(Model model) {
        model.addAttribute("list", employmentService.listAll());
        return "admin/employment-list";
    }

    /** 负责人审核：status 为 已确认 / 已退回。 */
    @PostMapping("/admin/employment/{id}/review")
    public String review(@PathVariable Long id,
                         @RequestParam String status,
                         @RequestParam(required = false) String comment) {
        employmentService.review(id, status, comment);
        return "redirect:/admin/employment";
    }

    /** 保存上传的证明材料到 uploads 目录，返回存储文件名。 */
    private String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            Path dir = Paths.get("uploads");
            Files.createDirectories(dir);
            String storedName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            file.transferTo(dir.resolve(storedName).toFile());
            return storedName;
        } catch (Exception e) {
            return file.getOriginalFilename();
        }
    }
}

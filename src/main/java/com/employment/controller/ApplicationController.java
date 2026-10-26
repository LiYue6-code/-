package com.employment.controller;

import com.employment.entity.JobApplication;
import com.employment.service.ApplicationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 岗位申请控制器：学生查看申请，负责人审核申请并登记面试/录用。
 */
@Controller
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/my/applications")
    public String my(HttpSession session, Model model) {
        Long studentId = (Long) session.getAttribute("userId");
        model.addAttribute("applications", applicationService.myApplications(studentId));
        return "my-applications";
    }

    @GetMapping("/admin/applications")
    public String adminList(Model model) {
        model.addAttribute("applications", applicationService.listAll());
        return "admin/application-list";
    }

    /** 审核申请：登记审核状态、面试结果、录用状态。 */
    @PostMapping("/admin/applications/{id}/review")
    public String review(@PathVariable Long id,
                         @RequestParam String status,
                         @RequestParam(required = false) String interviewResult,
                         @RequestParam(required = false) String hireStatus) {
        JobApplication application = new JobApplication();
        application.setId(id);
        application.setStatus(status);
        application.setInterviewResult(interviewResult);
        application.setHireStatus(hireStatus);
        applicationService.review(application);
        return "redirect:/admin/applications";
    }
}

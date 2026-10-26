package com.employment.controller;

import com.employment.entity.Job;
import com.employment.service.ApplicationService;
import com.employment.service.CompanyService;
import com.employment.service.JobService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 岗位控制器：学生端查询/申请，负责人端发布/下架。
 */
@Controller
public class JobController {

    private final JobService jobService;
    private final CompanyService companyService;
    private final ApplicationService applicationService;

    public JobController(JobService jobService, CompanyService companyService,
                         ApplicationService applicationService) {
        this.jobService = jobService;
        this.companyService = companyService;
        this.applicationService = applicationService;
    }

    /** 学生端岗位列表（可搜索）。 */
    @GetMapping("/jobs")
    public String list(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("jobs", jobService.search(keyword));
        model.addAttribute("keyword", keyword);
        return "job-list";
    }

    @GetMapping("/jobs/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("job", jobService.get(id));
        return "job-detail";
    }

    /** 学生申请岗位。 */
    @PostMapping("/jobs/{id}/apply")
    public String apply(@PathVariable Long id, HttpSession session, Model model) {
        Long studentId = (Long) session.getAttribute("userId");
        String error = applicationService.apply(id, studentId);
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("job", jobService.get(id));
            return "job-detail";
        }
        return "redirect:/my/applications";
    }

    /** 负责人端岗位列表。 */
    @GetMapping("/admin/jobs")
    public String adminList(Model model) {
        model.addAttribute("jobs", jobService.listAll());
        return "admin/job-list";
    }

    @GetMapping("/admin/jobs/new")
    public String newForm(Model model) {
        model.addAttribute("job", new Job());
        model.addAttribute("companies", companyService.list());
        return "admin/job-form";
    }

    @PostMapping("/admin/jobs")
    public String create(Job job) {
        jobService.create(job);
        return "redirect:/admin/jobs";
    }

    @PostMapping("/admin/jobs/{id}/offline")
    public String offline(@PathVariable Long id) {
        jobService.offline(id);
        return "redirect:/admin/jobs";
    }
}

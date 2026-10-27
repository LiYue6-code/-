package com.employment.controller;

import com.employment.entity.Job;
import com.employment.query.JobQuery;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    /** 学生端岗位列表：按关键字（岗位/单位）与单位筛选，分页展示。 */
    @GetMapping("/jobs")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long companyId,
                       @RequestParam(defaultValue = "1") int page,
                       HttpSession session,
                       Model model) {
        JobQuery query = new JobQuery();
        query.setKeyword(blankToNull(keyword));
        query.setCompanyId(companyId);
        query.setPage(Math.max(page, 1));
        query.setSize(10);

        Long studentId = (Long) session.getAttribute("userId");
        model.addAttribute("pageData", jobService.search(query));
        model.addAttribute("companies", companyService.list());
        model.addAttribute("keyword", keyword);
        model.addAttribute("companyId", companyId);
        model.addAttribute("appliedJobIds", applicationService.myAppliedJobIds(studentId));
        return "job-list";
    }

    /** 学生端岗位详情：标记是否已申请、是否仍在招聘。 */
    @GetMapping("/jobs/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        Long studentId = (Long) session.getAttribute("userId");
        Job job = jobService.getForStudent(id);
        model.addAttribute("job", job);
        model.addAttribute("applied", applicationService.hasApplied(id, studentId));
        return "job-detail";
    }

    /** 学生申请岗位。 */
    @PostMapping("/jobs/{id}/apply")
    public String apply(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Long studentId = (Long) session.getAttribute("userId");
        String error = applicationService.apply(id, studentId);
        if (error != null) {
            redirectAttributes.addFlashAttribute("err", error);
            return "redirect:/jobs/" + id;
        }
        redirectAttributes.addFlashAttribute("msg", "申请已提交，请在学校负责人审核后查看处理进度");
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

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

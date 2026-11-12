package com.employment.controller;

import com.employment.common.DateTimes;
import com.employment.entity.Job;
import com.employment.query.JobQuery;
import com.employment.service.ApplicationService;
import com.employment.service.CompanyService;
import com.employment.service.JobService;
import com.employment.service.ResumeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 岗位控制器：学生端查询/申请（关联简历）与意向推荐，负责人端发布/审核/上下架。
 */
@Controller
public class JobController {

    private final JobService jobService;
    private final CompanyService companyService;
    private final ApplicationService applicationService;
    private final ResumeService resumeService;

    public JobController(JobService jobService, CompanyService companyService,
                         ApplicationService applicationService, ResumeService resumeService) {
        this.jobService = jobService;
        this.companyService = companyService;
        this.applicationService = applicationService;
        this.resumeService = resumeService;
    }

    /** 学生端岗位列表：按关键字（岗位/单位）与单位筛选，分页展示，附求职意向推荐。 */
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
        model.addAttribute("recommendations", jobService.recommend(studentId));
        return "job-list";
    }

    /** 学生端岗位详情：标记是否已申请、是否仍在招聘，并提供简历选择。 */
    @GetMapping("/jobs/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        Long studentId = (Long) session.getAttribute("userId");
        Job job = jobService.getForStudent(id);
        model.addAttribute("job", job);
        model.addAttribute("applied", applicationService.hasApplied(id, studentId));
        model.addAttribute("resumes", resumeService.list(studentId));
        return "job-detail";
    }

    /** 学生申请岗位（可关联简历）。 */
    @PostMapping("/jobs/{id}/apply")
    public String apply(@PathVariable Long id,
                        @RequestParam(required = false) Long resumeId,
                        HttpSession session, RedirectAttributes redirectAttributes) {
        Long studentId = (Long) session.getAttribute("userId");
        String error = applicationService.apply(id, studentId, resumeId);
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
        model.addAttribute("companies", companyService.listApproved());
        return "admin/job-form";
    }

    @PostMapping("/admin/jobs")
    public String create(Job job, @RequestParam(required = false) String validUntil,
                         RedirectAttributes redirectAttributes) {
        job.setValidUntil(DateTimes.parse(validUntil));
        jobService.create(job);
        redirectAttributes.addFlashAttribute("msg", "岗位已发布，待审核通过后自动上架");
        return "redirect:/admin/jobs";
    }

    /** 岗位审核：通过 / 驳回。 */
    @PostMapping("/admin/jobs/{id}/review")
    public String review(@PathVariable Long id,
                         @RequestParam boolean passed,
                         @RequestParam(required = false) String comment,
                         RedirectAttributes redirectAttributes) {
        String error = jobService.review(id, passed, comment);
        return backToList(id, error, passed ? "审核通过，岗位已上架" : "已驳回", redirectAttributes);
    }

    /** 手动上架。 */
    @PostMapping("/admin/jobs/{id}/online")
    public String online(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String error = jobService.online(id);
        return backToList(id, error, "岗位已上架", redirectAttributes);
    }

    /** 手动下架。 */
    @PostMapping("/admin/jobs/{id}/offline")
    public String offline(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String error = jobService.offline(id);
        return backToList(id, error, "岗位已下架", redirectAttributes);
    }

    private String backToList(Long id, String error, String successMessage, RedirectAttributes redirectAttributes) {
        if (error != null) {
            redirectAttributes.addFlashAttribute("err", error);
        } else {
            redirectAttributes.addFlashAttribute("msg", successMessage);
        }
        return "redirect:/admin/jobs";
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
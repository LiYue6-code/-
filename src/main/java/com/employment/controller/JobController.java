package com.employment.controller;

import com.employment.entity.Job;
import com.employment.query.AdminJobQuery;
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

    /** 负责人端岗位列表：按关键字（岗位/单位）、招聘单位、状态筛选并分页。 */
    @GetMapping("/admin/jobs")
    public String adminList(@RequestParam(required = false) String keyword,
                            @RequestParam(required = false) Long companyId,
                            @RequestParam(required = false) String status,
                            @RequestParam(defaultValue = "1") int page,
                            Model model) {
        AdminJobQuery query = new AdminJobQuery();
        query.setKeyword(blankToNull(keyword));
        query.setCompanyId(companyId);
        query.setStatus(blankToNull(status));
        query.setPage(Math.max(page, 1));
        query.setSize(10);

        model.addAttribute("pageData", jobService.adminSearch(query));
        model.addAttribute("companies", companyService.list());
        model.addAttribute("keyword", keyword);
        model.addAttribute("companyId", companyId);
        model.addAttribute("status", status);
        return "admin/job-list";
    }

    /** 发布岗位表单：默认「招聘中」、招聘人数 1。 */
    @GetMapping("/admin/jobs/new")
    public String newForm(Model model) {
        Job job = new Job();
        job.setStatus("招聘中");
        job.setHeadcount(1);
        model.addAttribute("job", job);
        model.addAttribute("companies", companyService.list());
        return "admin/job-form";
    }

    /** 编辑岗位表单。 */
    @GetMapping("/admin/jobs/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Job job = jobService.get(id);
        if (job == null) {
            redirectAttributes.addFlashAttribute("err", "岗位不存在或已被删除");
            return "redirect:/admin/jobs";
        }
        model.addAttribute("job", job);
        model.addAttribute("companies", companyService.list());
        return "admin/job-form";
    }

    @PostMapping("/admin/jobs")
    public String create(Job job, Model model, RedirectAttributes redirectAttributes) {
        if (!companyExists(job.getCompanyId())) {
            return jobFormWithError(job, "请选择有效的招聘单位", model);
        }
        String error = jobService.create(job);
        if (error != null) {
            return jobFormWithError(job, error, model);
        }
        redirectAttributes.addFlashAttribute("msg", "岗位已发布");
        return "redirect:/admin/jobs";
    }

    @PostMapping("/admin/jobs/{id}/update")
    public String update(@PathVariable Long id, Job job, Model model, RedirectAttributes redirectAttributes) {
        job.setId(id);
        if (!companyExists(job.getCompanyId())) {
            return jobFormWithError(job, "请选择有效的招聘单位", model);
        }
        String error = jobService.update(job);
        if (error != null) {
            return jobFormWithError(job, error, model);
        }
        redirectAttributes.addFlashAttribute("msg", "岗位已更新");
        return "redirect:/admin/jobs";
    }

    /** 下架：先确认岗位存在且确实在招，避免“静默成功”。 */
    @PostMapping("/admin/jobs/{id}/offline")
    public String offline(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Job job = jobService.get(id);
        if (job == null) {
            redirectAttributes.addFlashAttribute("err", "岗位不存在或已被删除");
        } else if (!"招聘中".equals(job.getStatus())) {
            redirectAttributes.addFlashAttribute("err", "该岗位当前状态为「" + job.getStatus() + "」，无需下架");
        } else {
            jobService.offline(id);
            redirectAttributes.addFlashAttribute("msg", "岗位已下架");
        }
        return "redirect:/admin/jobs";
    }

    /** 上架：仅允许从「已截止」恢复为「招聘中」。 */
    @PostMapping("/admin/jobs/{id}/online")
    public String online(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Job job = jobService.get(id);
        if (job == null) {
            redirectAttributes.addFlashAttribute("err", "岗位不存在或已被删除");
        } else if (!"已截止".equals(job.getStatus())) {
            redirectAttributes.addFlashAttribute("err", "该岗位当前状态为「" + job.getStatus() + "」，无需上架");
        } else {
            jobService.online(id);
            redirectAttributes.addFlashAttribute("msg", "岗位已上架，学生端可重新申请");
        }
        return "redirect:/admin/jobs";
    }

    private boolean companyExists(Long companyId) {
        return companyId != null && companyService.get(companyId) != null;
    }

    private String jobFormWithError(Job job, String error, Model model) {
        model.addAttribute("job", job);
        model.addAttribute("error", error);
        model.addAttribute("companies", companyService.list());
        return "admin/job-form";
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

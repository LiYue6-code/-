package com.employment.controller;

import com.employment.common.CsvExporter;
import com.employment.common.DateTimes;
import com.employment.entity.JobApplication;
import com.employment.query.ApplicationQuery;
import com.employment.service.ApplicationFlow;
import com.employment.service.ApplicationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.employment.service.ApplicationFlow.Action;

/**
 * 岗位申请控制器。
 *
 * <p>学生端：查看本人申请与处理进度。
 * <p>负责人端：申请列表（筛选/分页/导出）、审核详情、初审、面试、录用登记。
 */
@Controller
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    // ==================== 学生端 ====================

    @GetMapping("/my/applications")
    public String my(HttpSession session, Model model) {
        Long studentId = (Long) session.getAttribute("userId");
        model.addAttribute("applications", applicationService.myApplications(studentId));
        return "my-applications";
    }

    /** 学生查看单条申请的处理进度（需求 ⑥）。 */
    @GetMapping("/my/applications/{id}/progress")
    public String myProgress(@PathVariable Long id, HttpSession session, Model model) {
        Long studentId = (Long) session.getAttribute("userId");
        JobApplication application = applicationService.myDetail(id, studentId);
        model.addAttribute("title", "岗位申请处理进度");
        model.addAttribute("subject", application.getJobTitle() + " · " + application.getCompanyName());
        model.addAttribute("current", ApplicationFlow.describe(application.getStatus(),
                application.getInterviewResult(), application.getHireStatus()));
        model.addAttribute("logs", applicationService.timeline(id));
        return "my-progress";
    }

    // ==================== 负责人端：列表 ====================

    @GetMapping("/admin/applications")
    public String adminList(@RequestParam(required = false) String keyword,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                            @RequestParam(defaultValue = "1") int page,
                            Model model) {
        ApplicationQuery query = buildQuery(keyword, status, from, to, page);
        model.addAttribute("pageData", applicationService.search(query));
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "admin/application-list";
    }

    /** 导出当前筛选条件下的申请列表。 */
    @GetMapping("/admin/applications/export")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<JobApplication> list = applicationService.export(buildQuery(keyword, status, from, to, 1));

        CsvExporter csv = new CsvExporter();
        csv.row("学生", "学号", "岗位", "招聘单位", "申请时间", "初审状态", "面试时间", "面试结果", "录用状态", "审核人", "审核时间");
        for (JobApplication a : list) {
            csv.row(a.getStudentName(),
                    a.getStudentNo(),
                    a.getJobTitle(),
                    a.getCompanyName(),
                    a.getApplyTime() == null ? "" : a.getApplyTime().toString().replace('T', ' '),
                    a.getStatus(),
                    a.getInterviewTime() == null ? "" : a.getInterviewTime().toString().replace('T', ' '),
                    a.getInterviewResult(),
                    a.getHireStatus(),
                    a.getReviewerName(),
                    a.getReviewTime() == null ? "" : a.getReviewTime().toString().replace('T', ' '));
        }
        return csv.asResponse("岗位申请审核表_" + LocalDate.now() + ".csv");
    }
    // ==================== 负责人端：详情与审核 ====================

    @GetMapping("/admin/applications/{id}")
    public String detail(@PathVariable Long id, Model model) {
        JobApplication application = applicationService.detail(id);
        String status = application.getStatus();
        String interviewResult = application.getInterviewResult();
        String hireStatus = application.getHireStatus();

        model.addAttribute("app", application);
        model.addAttribute("logs", applicationService.timeline(id));
        // 由状态机统一判断各操作是否可用，页面据此显示/隐藏表单
        model.addAttribute("canApprove", ApplicationFlow.check(Action.APPROVE, status, interviewResult, hireStatus) == null);
        model.addAttribute("canReject", ApplicationFlow.check(Action.REJECT, status, interviewResult, hireStatus) == null);
        model.addAttribute("canSchedule", ApplicationFlow.check(Action.SCHEDULE, status, interviewResult, hireStatus) == null);
        model.addAttribute("canInterview", ApplicationFlow.check(Action.INTERVIEW_PASS, status, interviewResult, hireStatus) == null);
        model.addAttribute("canHire", ApplicationFlow.check(Action.HIRE, status, interviewResult, hireStatus) == null);
        model.addAttribute("canNoHire", ApplicationFlow.check(Action.NO_HIRE, status, interviewResult, hireStatus) == null);
        return "admin/application-detail";
    }

    @PostMapping("/admin/applications/{id}/approve")
    public String approve(@PathVariable Long id,
                          @RequestParam(required = false) String comment,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        String error = applicationService.approve(id, comment,
                (Long) session.getAttribute("userId"), (String) session.getAttribute("userName"));
        return back(id, error, "初审通过，已通知学生进入面试环节", redirectAttributes);
    }

    @PostMapping("/admin/applications/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam(required = false) String reason,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        String error = applicationService.reject(id, reason,
                (Long) session.getAttribute("userId"), (String) session.getAttribute("userName"));
        return back(id, error, "已登记初审不通过", redirectAttributes);
    }

    @PostMapping("/admin/applications/{id}/schedule")
    public String schedule(@PathVariable Long id,
                           @RequestParam(required = false) String interviewTime,
                           @RequestParam(required = false) String place,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        LocalDateTime time = DateTimes.parse(interviewTime);
        String error = applicationService.schedule(id, time, place,
                (Long) session.getAttribute("userId"), (String) session.getAttribute("userName"));
        return back(id, error, "面试安排已登记", redirectAttributes);
    }

    @PostMapping("/admin/applications/{id}/interview")
    public String interview(@PathVariable Long id,
                            @RequestParam boolean passed,
                            @RequestParam(required = false) String comment,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        String error = applicationService.interviewResult(id, passed, comment,
                (Long) session.getAttribute("userId"), (String) session.getAttribute("userName"));
        return back(id, error, passed ? "已登记面试通过" : "已登记面试未通过", redirectAttributes);
    }

    @PostMapping("/admin/applications/{id}/hire")
    public String hire(@PathVariable Long id,
                       @RequestParam boolean hired,
                       @RequestParam(required = false) String comment,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        String error = applicationService.hire(id, hired, comment,
                (Long) session.getAttribute("userId"), (String) session.getAttribute("userName"));
        return back(id, error, hired ? "已登记为录用" : "已登记为未录用", redirectAttributes);
    }

    // ==================== 内部方法 ====================

    private String back(Long id, String error, String successMessage, RedirectAttributes redirectAttributes) {
        if (error != null) {
            redirectAttributes.addFlashAttribute("err", error);
        } else {
            redirectAttributes.addFlashAttribute("msg", successMessage);
        }
        return "redirect:/admin/applications/" + id;
    }

    private static ApplicationQuery buildQuery(String keyword, String status,
                                               LocalDate from, LocalDate to, int page) {
        ApplicationQuery query = new ApplicationQuery();
        query.setKeyword(blankToNull(keyword));
        query.setStatus(blankToNull(status));
        query.setFrom(from);
        query.setTo(to);
        query.setPage(Math.max(page, 1));
        query.setSize(10);
        return query;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

package com.employment.controller;

import com.employment.entity.EmploymentInfo;
import com.employment.entity.JobApplication;
import com.employment.service.ApplicationFlow;
import com.employment.service.ApplicationService;
import com.employment.service.EmploymentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 学生端「处理进度查询」入口。
 *
 * <p>把本人的岗位申请与就业上报汇总到一页，学生可以直接看到每条事项的当前状态，
 * 再点进各自的详情页查看完整处理时间线。
 */
@Controller
public class ProgressController {

    private final ApplicationService applicationService;
    private final EmploymentService employmentService;

    public ProgressController(ApplicationService applicationService, EmploymentService employmentService) {
        this.applicationService = applicationService;
        this.employmentService = employmentService;
    }

    @GetMapping("/my/progress")
    public String progress(HttpSession session, Model model) {
        Long studentId = (Long) session.getAttribute("userId");
        List<JobApplication> applications = applicationService.myApplications(studentId);
        List<EmploymentInfo> employments = employmentService.myList(studentId);

        model.addAttribute("applications", applications);
        model.addAttribute("employments", employments);
        model.addAttribute("returned", employments.stream()
                .filter(EmploymentInfo::isReturned)
                .findFirst().orElse(null));

        model.addAttribute("applicationCount", applications.size());
        model.addAttribute("applicationWaiting", applications.stream()
                .filter(a -> ApplicationFlow.WAIT.equals(a.getStatus())).count());
        model.addAttribute("applicationPassed", applications.stream()
                .filter(a -> ApplicationFlow.PASSED.equals(a.getStatus())).count());
        model.addAttribute("hiredCount", applications.stream()
                .filter(a -> ApplicationFlow.HIRED.equals(a.getHireStatus())).count());
        return "my-progress-list";
    }
}

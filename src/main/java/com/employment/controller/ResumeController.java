package com.employment.controller;

import com.employment.entity.JobIntention;
import com.employment.entity.Resume;
import com.employment.service.ResumeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 简历与求职意向控制器（学生端）。
 */
@Controller
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    /** 简历列表 + 求职意向总览。 */
    @GetMapping("/my/resume")
    public String list(HttpSession session, Model model) {
        Long studentId = (Long) session.getAttribute("userId");
        model.addAttribute("resumes", resumeService.list(studentId));
        JobIntention intention = resumeService.getIntention(studentId);
        model.addAttribute("intention", intention == null ? new JobIntention() : intention);
        return "my-resumes";
    }

    @GetMapping("/my/resume/new")
    public String newForm(Model model) {
        model.addAttribute("resume", new Resume());
        return "resume-form";
    }

    @PostMapping("/my/resume")
    public String create(Resume resume, HttpSession session, RedirectAttributes redirectAttributes) {
        Long studentId = (Long) session.getAttribute("userId");
        resume.setStudentId(studentId);
        String error = resumeService.validate(resume);
        if (error != null) {
            redirectAttributes.addFlashAttribute("err", error);
            return "redirect:/my/resume/new";
        }
        resumeService.create(resume);
        redirectAttributes.addFlashAttribute("msg", "简历已创建");
        return "redirect:/my/resume";
    }

    @GetMapping("/my/resume/{id}/edit")
    public String edit(@PathVariable Long id, HttpSession session, Model model) {
        Long studentId = (Long) session.getAttribute("userId");
        model.addAttribute("resume", resumeService.get(id));
        model.addAttribute("studentId", studentId);
        return "resume-form";
    }

    @PostMapping("/my/resume/{id}/update")
    public String update(@PathVariable Long id, Resume form, HttpSession session,
                         RedirectAttributes redirectAttributes) {
        Long studentId = (Long) session.getAttribute("userId");
        String error = resumeService.validate(form);
        if (error != null) {
            redirectAttributes.addFlashAttribute("err", error);
            return "redirect:/my/resume/" + id + "/edit";
        }
        resumeService.update(id, studentId, form);
        redirectAttributes.addFlashAttribute("msg", "简历已更新");
        return "redirect:/my/resume";
    }

    @PostMapping("/my/resume/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Long studentId = (Long) session.getAttribute("userId");
        resumeService.delete(id, studentId);
        redirectAttributes.addFlashAttribute("msg", "简历已删除");
        return "redirect:/my/resume";
    }

    @PostMapping("/my/resume/{id}/default")
    public String setDefault(@PathVariable Long id, HttpSession session) {
        Long studentId = (Long) session.getAttribute("userId");
        resumeService.setDefault(id, studentId);
        return "redirect:/my/resume";
    }

    /** 保存求职意向。 */
    @PostMapping("/my/intention")
    public String saveIntention(JobIntention intention, HttpSession session, RedirectAttributes redirectAttributes) {
        Long studentId = (Long) session.getAttribute("userId");
        intention.setStudentId(studentId);
        resumeService.saveIntention(intention);
        redirectAttributes.addFlashAttribute("msg", "求职意向已保存");
        return "redirect:/my/resume";
    }
}
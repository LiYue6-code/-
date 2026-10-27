package com.employment.controller;

import com.employment.common.CsvExporter;
import com.employment.entity.EmploymentInfo;
import com.employment.query.EmploymentQuery;
import com.employment.service.EmploymentService;
import com.employment.service.FileStorageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * 就业信息控制器。
 *
 * <p>学生端：上报就业信息与证明材料、被退回后修改重报、查看处理进度、下载本人证明材料。
 * <p>负责人端：上报列表（筛选/分页/导出）、审核详情、证明材料下载、确认通过/退回补正。
 */
@Controller
public class EmploymentController {

    private final EmploymentService employmentService;
    private final FileStorageService fileStorageService;

    public EmploymentController(EmploymentService employmentService, FileStorageService fileStorageService) {
        this.employmentService = employmentService;
        this.fileStorageService = fileStorageService;
    }

    // ==================== 学生端 ====================

    @GetMapping("/my/employment")
    public String my(HttpSession session, Model model) {
        Long studentId = (Long) session.getAttribute("userId");
        List<EmploymentInfo> list = employmentService.myList(studentId);
        boolean hasActive = list.stream()
                .anyMatch(e -> "待审核".equals(e.getStatus()) || "已确认".equals(e.getStatus()));
        EmploymentInfo returned = list.stream()
                .filter(e -> "已退回".equals(e.getStatus()))
                .findFirst().orElse(null);
        model.addAttribute("list", list);
        // 已退回时只显示补正表单；已有活动记录时只提示，避免同时出现两张可提交的表单
        model.addAttribute("canReport", !hasActive && returned == null);
        model.addAttribute("returned", returned);
        model.addAttribute("info", new EmploymentInfo());
        return "my-employment";
    }

    /** 学生上报就业信息及证明材料。 */
    @PostMapping("/my/employment")
    public String report(EmploymentInfo info,
                         @RequestParam(required = false) MultipartFile proofFile,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        Long studentId = (Long) session.getAttribute("userId");
        info.setStudentId(studentId);
        if (proofFile != null && !proofFile.isEmpty()) {
            info.setProofFile(fileStorageService.store(proofFile));
            info.setProofName(FileStorageService.originalName(proofFile));
        }
        String error = employmentService.report(info);
        if (error != null) {
            redirectAttributes.addFlashAttribute("err", error);
        } else {
            redirectAttributes.addFlashAttribute("msg", "就业信息已提交，等待负责人审核");
        }
        return "redirect:/my/employment";
    }

    /** 学生查看单条上报的处理进度。 */
    @GetMapping("/my/employment/{id}/progress")
    public String myProgress(@PathVariable Long id, HttpSession session, Model model) {
        Long studentId = (Long) session.getAttribute("userId");
        EmploymentInfo info = employmentService.myDetail(id, studentId);
        model.addAttribute("title", "就业信息审核进度");
        model.addAttribute("subject", info.getCompanyName() + " · " + (info.getPosition() == null ? "岗位未填" : info.getPosition()));
        model.addAttribute("current", info.getStatus());
        model.addAttribute("logs", employmentService.timeline(id));
        return "my-progress";
    }

    /** 学生被退回后修改并重新上报。 */
    @PostMapping("/my/employment/{id}/resubmit")
    public String resubmit(@PathVariable Long id,
                           EmploymentInfo form,
                           @RequestParam(required = false) MultipartFile proofFile,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        Long studentId = (Long) session.getAttribute("userId");
        String stored = null;
        String original = null;
        if (proofFile != null && !proofFile.isEmpty()) {
            stored = fileStorageService.store(proofFile);
            original = FileStorageService.originalName(proofFile);
        }
        String error = employmentService.resubmit(id, form, studentId, stored, original);
        if (error != null) {
            redirectAttributes.addFlashAttribute("err", error);
        } else {
            redirectAttributes.addFlashAttribute("msg", "已重新提交，等待负责人审核");
        }
        return "redirect:/my/employment";
    }

    /** 学生下载本人证明材料。 */
    @GetMapping("/my/employment/{id}/proof")
    public ResponseEntity<Resource> myProof(@PathVariable Long id, HttpSession session) {
        Long studentId = (Long) session.getAttribute("userId");
        return proofResponse(employmentService.myDetail(id, studentId));
    }

    // ==================== 负责人端 ====================

    @GetMapping("/admin/employment")
    public String adminList(@RequestParam(required = false) String keyword,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                            @RequestParam(defaultValue = "1") int page,
                            Model model) {
        EmploymentQuery query = buildQuery(keyword, status, from, to, page);
        model.addAttribute("pageData", employmentService.search(query));
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "admin/employment-list";
    }

    @GetMapping("/admin/employment/export")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<EmploymentInfo> list = employmentService.export(buildQuery(keyword, status, from, to, 1));
        CsvExporter csv = new CsvExporter();
        csv.row("学生", "学号", "就业单位", "岗位", "薪资", "证明材料", "状态", "审核意见", "上报时间", "审核人", "审核时间");
        for (EmploymentInfo e : list) {
            csv.row(e.getStudentName(), e.getStudentNo(), e.getCompanyName(), e.getPosition(), e.getSalary(),
                    e.getProofName() == null ? e.getProofFile() : e.getProofName(),
                    e.getStatus(), e.getReviewComment(),
                    e.getSubmitTime() == null ? "" : e.getSubmitTime().toString().replace('T', ' '),
                    e.getReviewerName(),
                    e.getReviewTime() == null ? "" : e.getReviewTime().toString().replace('T', ' '));
        }
        return csv.asResponse("就业信息审核表_" + LocalDate.now() + ".csv");
    }

    @GetMapping("/admin/employment/{id}")
    public String detail(@PathVariable Long id, Model model) {
        EmploymentInfo info = employmentService.detail(id);
        model.addAttribute("info", info);
        model.addAttribute("logs", employmentService.timeline(id));
        model.addAttribute("canConfirm", "待审核".equals(info.getStatus()));
        model.addAttribute("canReturn", "待审核".equals(info.getStatus()));
        return "admin/employment-detail";
    }

    /** 审核：confirmed=true 确认通过，false 退回补正（必须填写补正内容）。 */
    @PostMapping("/admin/employment/{id}/review")
    public String review(@PathVariable Long id,
                         @RequestParam boolean confirmed,
                         @RequestParam(required = false) String comment,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        String error = employmentService.review(id, confirmed, comment,
                (Long) session.getAttribute("userId"), (String) session.getAttribute("userName"));
        if (error != null) {
            redirectAttributes.addFlashAttribute("err", error);
        } else {
            redirectAttributes.addFlashAttribute("msg", confirmed ? "已确认该就业信息" : "已退回，等待学生补正");
        }
        return "redirect:/admin/employment/" + id;
    }

    /** 负责人下载/查看证明材料。 */
    @GetMapping("/admin/employment/{id}/proof")
    public ResponseEntity<Resource> proof(@PathVariable Long id) {
        return proofResponse(employmentService.detail(id));
    }

    // ==================== 内部方法 ====================

    private ResponseEntity<Resource> proofResponse(EmploymentInfo info) {
        if (info.getProofFile() == null || info.getProofFile().isBlank()) {
            throw new com.employment.common.BizException("该记录没有上传证明材料");
        }
        Path path = fileStorageService.resolve(info.getProofFile());
        String downloadName = info.getProofName() == null || info.getProofName().isBlank()
                ? info.getProofFile() : info.getProofName();
        MediaType mediaType = MediaTypeFactory.getMediaType(downloadName)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, CsvExporter.attachment(downloadName))
                .contentType(mediaType)
                .body(new FileSystemResource(path));
    }

    private static EmploymentQuery buildQuery(String keyword, String status,
                                              LocalDate from, LocalDate to, int page) {
        EmploymentQuery query = new EmploymentQuery();
        query.setKeyword(keyword == null || keyword.isBlank() ? null : keyword.trim());
        query.setStatus(status == null || status.isBlank() ? null : status.trim());
        query.setFrom(from);
        query.setTo(to);
        query.setPage(Math.max(page, 1));
        query.setSize(10);
        return query;
    }
}

package com.employment.controller;

import com.employment.entity.Company;
import com.employment.service.CompanyService;
import com.employment.service.FileStorageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 招聘单位管理（负责人端）：基本信息维护 + 资质材料上传与资质审核、信用状态标记。
 */
@Controller
@RequestMapping("/admin/companies")
public class CompanyController {

    private final CompanyService companyService;
    private final FileStorageService fileStorageService;

    public CompanyController(CompanyService companyService, FileStorageService fileStorageService) {
        this.companyService = companyService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("companies", companyService.list());
        return "admin/company-list";
    }

    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("company", new Company());
        return "admin/company-form";
    }

    @PostMapping
    public String create(Company company,
                         @RequestParam(required = false) MultipartFile qualificationFile,
                         RedirectAttributes redirectAttributes) {
        applyQualification(company, qualificationFile);
        companyService.create(company);
        redirectAttributes.addFlashAttribute("msg", "单位已新增，待资质审核通过后方可发布岗位");
        return "redirect:/admin/companies";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("company", companyService.get(id));
        return "admin/company-form";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id, Company company,
                         @RequestParam(required = false) MultipartFile qualificationFile,
                         RedirectAttributes redirectAttributes) {
        Company existing = companyService.get(id);
        if (qualificationFile != null && !qualificationFile.isEmpty()) {
            applyQualification(company, qualificationFile);
        } else {
            // 未重新上传时沿用原有资质材料
            company.setQualificationFile(existing == null ? null : existing.getQualificationFile());
            company.setQualificationName(existing == null ? null : existing.getQualificationName());
        }
        company.setId(id);
        companyService.update(company);
        redirectAttributes.addFlashAttribute("msg", "单位信息已更新");
        return "redirect:/admin/companies";
    }

    /** 资质审核：通过 / 驳回。 */
    @PostMapping("/{id}/review")
    public String review(@PathVariable Long id,
                         @RequestParam boolean passed,
                         @RequestParam(required = false) String comment,
                         RedirectAttributes redirectAttributes) {
        String error = companyService.review(id, passed, comment);
        if (error != null) {
            redirectAttributes.addFlashAttribute("err", error);
        } else {
            redirectAttributes.addFlashAttribute("msg", passed ? "资质审核已通过" : "已驳回该单位资质");
        }
        return "redirect:/admin/companies";
    }

    /** 信用状态标记：正常 / 异常。 */
    @PostMapping("/{id}/credit")
    public String credit(@PathVariable Long id,
                         @RequestParam String creditStatus,
                         RedirectAttributes redirectAttributes) {
        companyService.updateCreditStatus(id, creditStatus);
        redirectAttributes.addFlashAttribute("msg", "信用状态已更新");
        return "redirect:/admin/companies";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        companyService.delete(id);
        return "redirect:/admin/companies";
    }

    /** 保存上传的资质材料：FileStorageService 负责 UUID 重命名与格式/大小校验。 */
    private void applyQualification(Company company, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }
        company.setQualificationFile(fileStorageService.store(file));
        company.setQualificationName(FileStorageService.originalName(file));
    }
}
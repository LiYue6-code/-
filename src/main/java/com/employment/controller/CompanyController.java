package com.employment.controller;

import com.employment.entity.Company;
import com.employment.service.CompanyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 招聘单位管理（负责人端）。
 */
@Controller
@RequestMapping("/admin/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
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
    public String create(Company company, Model model) {
        String error = companyService.create(company);
        if (error != null) {
            model.addAttribute("company", company);
            model.addAttribute("error", error);
            return "admin/company-form";
        }
        return "redirect:/admin/companies";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Company company = companyService.get(id);
        if (company == null) {
            redirectAttributes.addFlashAttribute("err", "单位不存在或已被删除");
            return "redirect:/admin/companies";
        }
        model.addAttribute("company", company);
        return "admin/company-form";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id, Company company, Model model) {
        company.setId(id);
        String error = companyService.update(company);
        if (error != null) {
            model.addAttribute("company", company);
            model.addAttribute("error", error);
            return "admin/company-form";
        }
        return "redirect:/admin/companies";
    }

    /** 删除单位：该单位下还有岗位时会被拒绝，并在列表页提示原因。 */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String error = companyService.delete(id);
        if (error != null) {
            redirectAttributes.addFlashAttribute("err", error);
        } else {
            redirectAttributes.addFlashAttribute("msg", "单位已删除");
        }
        return "redirect:/admin/companies";
    }
}

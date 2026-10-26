package com.employment.controller;

import com.employment.entity.Company;
import com.employment.service.CompanyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public String create(Company company) {
        companyService.create(company);
        return "redirect:/admin/companies";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("company", companyService.get(id));
        return "admin/company-form";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id, Company company) {
        company.setId(id);
        companyService.update(company);
        return "redirect:/admin/companies";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        companyService.delete(id);
        return "redirect:/admin/companies";
    }
}

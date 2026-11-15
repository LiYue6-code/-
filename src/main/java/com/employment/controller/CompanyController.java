package com.employment.controller;

import com.employment.common.BizException;
import com.employment.common.CsvExporter;
import com.employment.entity.Company;
import com.employment.service.CompanyService;
import com.employment.service.FileStorageService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Path;

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

    /**
     * 阻止表单字段直接绑定到实体上的「文件/审核」属性。
     *
     * <p>表单的文件域名为 qualificationFile，与公司实体的同名字段冲突：
     * Spring 会尝试把 MultipartFile 绑定到 String 属性上，抛出绑定异常并返回 400。
     * 文件统一由方法参数 {@code @RequestParam MultipartFile} 接收，这里禁止绑定。
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("qualificationFile", "qualificationName",
                "reviewStatus", "reviewComment", "creditStatus");
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

    /** 资质审核：通过 / 驳回。 */    @PostMapping("/{id}/review")
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

    /**
     * 下载或在线查看资质材料。
     *
     * <p>PDF 与图片以 inline 方式返回，浏览器直接打开，便于负责人审核时核对；
     * 其他类型（Word 等）以下载方式返回。文件名使用数据库中保存的原始文件名。
     */
    @GetMapping("/{id}/qualification")
    public ResponseEntity<Resource> qualification(@PathVariable Long id) {
        Company company = companyService.get(id);
        if (company == null) {
            throw new BizException("单位不存在或已被删除");
        }
        if (company.getQualificationFile() == null || company.getQualificationFile().isBlank()) {
            throw new BizException("该单位尚未上传资质材料，无法查看");
        }
        return fileResponse(company.getQualificationFile(), company.getQualificationName());
    }

    private ResponseEntity<Resource> fileResponse(String storedName, String originalName) {
        Path path = fileStorageService.resolve(storedName);
        String downloadName = (originalName == null || originalName.isBlank()) ? storedName : originalName;
        MediaType mediaType = MediaTypeFactory.getMediaType(downloadName)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        boolean inline = MediaType.APPLICATION_PDF.equals(mediaType) || "image".equals(mediaType.getType());
        String disposition = CsvExporter.attachment(downloadName)
                .replaceFirst("^attachment", inline ? "inline" : "attachment");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .contentType(mediaType)
                .body(new FileSystemResource(path));
    }
}
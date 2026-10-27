package com.employment.service;

import com.employment.entity.Company;
import com.employment.mapper.CompanyMapper;
import com.employment.mapper.JobMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 招聘单位业务。
 *
 * <p>负责人端增删改查；删除前会检查该单位下是否还有岗位，避免产生孤儿岗位。
 */
@Service
public class CompanyService {

    private final CompanyMapper companyMapper;
    private final JobMapper jobMapper;

    public CompanyService(CompanyMapper companyMapper, JobMapper jobMapper) {
        this.companyMapper = companyMapper;
        this.jobMapper = jobMapper;
    }

    /** 全量单位列表（学生端筛选下拉与岗位发布表单共用，保持无参语义）。 */
    public List<Company> list() {
        return companyMapper.findAll();
    }

    public Company get(Long id) {
        return companyMapper.findById(id);
    }

    /**
     * 新增单位。
     *
     * @return 校验通过返回 {@code null}；否则返回中文提示且不写库。
     */
    public String create(Company company) {
        String error = normalize(company);
        if (error != null) {
            return error;
        }
        companyMapper.insert(company);
        return null;
    }

    /**
     * 编辑单位。
     *
     * @return 校验通过返回 {@code null}；否则返回中文提示且不写库。
     */
    public String update(Company company) {
        if (company.getId() == null || companyMapper.findById(company.getId()) == null) {
            return "单位不存在或已被删除";
        }
        String error = normalize(company);
        if (error != null) {
            return error;
        }
        companyMapper.update(company);
        return null;
    }

    /**
     * 删除单位：该单位下还有岗位时拒绝删除。
     *
     * @return 删除成功返回 {@code null}；否则返回中文提示且不写库。
     */
    public String delete(Long id) {
        if (id == null || companyMapper.findById(id) == null) {
            return "单位不存在或已被删除";
        }
        int jobCount = jobMapper.countByCompany(id);
        if (jobCount > 0) {
            return "该单位下还有 " + jobCount + " 个岗位，无法删除；请先在「岗位管理」中处理这些岗位";
        }
        companyMapper.deleteById(id);
        return null;
    }

    /**
     * 字段整理与校验：去空格、按表结构长度校验。
     *
     * @return {@code null} 表示通过；否则返回中文错误提示
     */
    private String normalize(Company company) {
        company.setName(trim(company.getName()));
        if (company.getName().isEmpty()) {
            return "单位名称不能为空";
        }
        if (company.getName().length() > 100) {
            return "单位名称不能超过 100 字";
        }

        company.setType(trimToNull(company.getType()));
        if (company.getType() != null && company.getType().length() > 50) {
            return "单位性质不能超过 50 字";
        }

        company.setContact(trimToNull(company.getContact()));
        if (company.getContact() != null && company.getContact().length() > 50) {
            return "联系人不能超过 50 字";
        }

        company.setPhone(trimToNull(company.getPhone()));
        if (company.getPhone() != null && company.getPhone().length() > 20) {
            return "联系电话不能超过 20 字";
        }

        company.setAddress(trimToNull(company.getAddress()));
        if (company.getAddress() != null && company.getAddress().length() > 200) {
            return "单位地址不能超过 200 字";
        }

        company.setDescription(trimToNull(company.getDescription()));
        if (company.getDescription() != null && company.getDescription().length() > 500) {
            return "单位简介不能超过 500 字";
        }
        return null;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

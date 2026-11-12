package com.employment.service;

import com.employment.common.BizException;
import com.employment.entity.Company;
import com.employment.mapper.CompanyMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 招聘单位业务：基本信息维护、资质材料与资质审核、信用状态标记。
 */
@Service
public class CompanyService {

    private final CompanyMapper companyMapper;

    public CompanyService(CompanyMapper companyMapper) {
        this.companyMapper = companyMapper;
    }

    public List<Company> list() {
        return companyMapper.findAll();
    }

    /** 仅返回资质审核通过的单位（用于岗位发布下拉选择）。 */
    public List<Company> listApproved() {
        return companyMapper.findAllApproved();
    }

    public Company get(Long id) {
        return companyMapper.findById(id);
    }

    public void create(Company company) {
        companyMapper.insert(company);
    }

    public void update(Company company) {
        companyMapper.update(company);
    }

    /** 资质审核：通过 / 驳回（驳回需填写意见）。 */
    public String review(Long id, boolean passed, String comment) {
        Company company = requireForUpdate(id);
        String error = CompanyFlow.checkReview(company.getReviewStatus());
        if (error != null) {
            return error;
        }
        if (!passed && (comment == null || comment.isBlank())) {
            return "驳回时必须填写审核意见";
        }
        String reviewStatus = passed ? CompanyFlow.REVIEW_PASSED : CompanyFlow.REVIEW_REJECTED;
        companyMapper.review(id, reviewStatus, comment == null ? null : comment.trim());
        return null;
    }

    /** 信用状态标记：正常 / 异常。 */
    public void updateCreditStatus(Long id, String creditStatus) {
        requireForUpdate(id);
        if (!CompanyFlow.CREDIT_NORMAL.equals(creditStatus) && !CompanyFlow.CREDIT_ABNORMAL.equals(creditStatus)) {
            throw new BizException("信用状态取值不合法");
        }
        companyMapper.updateCreditStatus(id, creditStatus);
    }

    public void delete(Long id) {
        companyMapper.deleteById(id);
    }

    private Company requireForUpdate(Long id) {
        Company company = companyMapper.findById(id);
        if (company == null) {
            throw new BizException("单位不存在或已被删除");
        }
        return company;
    }
}
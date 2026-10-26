package com.employment.service;

import com.employment.entity.Company;
import com.employment.mapper.CompanyMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 招聘单位业务。
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

    public Company get(Long id) {
        return companyMapper.findById(id);
    }

    public void create(Company company) {
        companyMapper.insert(company);
    }

    public void update(Company company) {
        companyMapper.update(company);
    }

    public void delete(Long id) {
        companyMapper.deleteById(id);
    }
}

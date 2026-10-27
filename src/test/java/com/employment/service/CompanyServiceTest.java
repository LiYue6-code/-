package com.employment.service;

import com.employment.entity.Company;
import com.employment.mapper.CompanyMapper;
import com.employment.mapper.JobMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 招聘单位维护单元测试：字段校验与删除保护。
 */
@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyMapper companyMapper;

    @Mock
    private JobMapper jobMapper;

    @InjectMocks
    private CompanyService companyService;

    private Company company() {
        Company company = new Company();
        company.setName("某信息技术有限公司");
        return company;
    }

    @Test
    @DisplayName("新增单位：名称合法时写库")
    void createSavesCompany() {
        Company company = company();

        assertNull(companyService.create(company));
        verify(companyMapper).insert(company);
    }

    @Test
    @DisplayName("新增单位：名称为空时拒绝")
    void createRejectsBlankName() {
        Company company = company();
        company.setName("  ");

        assertEquals("单位名称不能为空", companyService.create(company));
        verify(companyMapper, never()).insert(any());
    }

    @Test
    @DisplayName("新增单位：名称超过 100 字时拒绝")
    void createRejectsTooLongName() {
        Company company = company();
        company.setName("单".repeat(101));

        assertEquals("单位名称不能超过 100 字", companyService.create(company));
        verify(companyMapper, never()).insert(any());
    }

    @Test
    @DisplayName("新增单位：联系电话超过 20 字时拒绝")
    void createRejectsTooLongPhone() {
        Company company = company();
        company.setPhone("1".repeat(21));

        assertEquals("联系电话不能超过 20 字", companyService.create(company));
        verify(companyMapper, never()).insert(any());
    }

    @Test
    @DisplayName("删除单位：名下还有岗位时拒绝删除")
    void deleteRejectedWhenJobsExist() {
        when(companyMapper.findById(1L)).thenReturn(company());
        when(jobMapper.countByCompany(1L)).thenReturn(2);

        String error = companyService.delete(1L);

        assertTrue(error.contains("2 个岗位"));
        verify(companyMapper, never()).deleteById(any());
    }

    @Test
    @DisplayName("删除单位：名下没有岗位时允许删除")
    void deleteSucceedsWhenNoJobs() {
        when(companyMapper.findById(1L)).thenReturn(company());
        when(jobMapper.countByCompany(1L)).thenReturn(0);

        assertNull(companyService.delete(1L));
        verify(companyMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除单位：单位不存在时提示且不写库")
    void deleteRejectsMissingCompany() {
        when(companyMapper.findById(9L)).thenReturn(null);

        assertEquals("单位不存在或已被删除", companyService.delete(9L));
        verify(companyMapper, never()).deleteById(any());
    }

    @Test
    @DisplayName("编辑单位：单位不存在时提示且不写库")
    void updateRejectsMissingCompany() {
        Company company = company();
        company.setId(9L);
        when(companyMapper.findById(9L)).thenReturn(null);

        assertEquals("单位不存在或已被删除", companyService.update(company));
        verify(companyMapper, never()).update(any());
    }
}

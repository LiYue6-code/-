package com.employment.service;

import com.employment.entity.Job;
import com.employment.mapper.JobMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 负责人端岗位维护单元测试：发布/编辑的字段校验、默认值、上下架。
 *
 * <p>学生端检索与下架行为仍由 {@link JobServiceTest} 覆盖，这里不重复。
 */
@ExtendWith(MockitoExtension.class)
class JobAdminServiceTest {

    @Mock
    private JobMapper jobMapper;

    @InjectMocks
    private JobService jobService;

    private Job job() {
        Job job = new Job();
        job.setCompanyId(1L);
        job.setTitle("Java 开发工程师");
        return job;
    }

    @Test
    @DisplayName("发布岗位：未填人数与状态时补默认值 1 / 招聘中")
    void createFillsDefaults() {
        Job job = job();

        assertNull(jobService.create(job));

        assertEquals(1, job.getHeadcount());
        assertEquals("招聘中", job.getStatus());
        verify(jobMapper).insert(job);
    }

    @Test
    @DisplayName("发布岗位：未选择招聘单位时拒绝")
    void createRejectsMissingCompany() {
        Job job = job();
        job.setCompanyId(null);

        assertEquals("请选择招聘单位", jobService.create(job));
        verify(jobMapper, never()).insert(any());
    }

    @Test
    @DisplayName("发布岗位：名称为空时拒绝")
    void createRejectsBlankTitle() {
        Job job = job();
        job.setTitle("   ");

        assertEquals("岗位名称不能为空", jobService.create(job));
        verify(jobMapper, never()).insert(any());
    }

    @Test
    @DisplayName("发布岗位：名称超过 100 字时拒绝")
    void createRejectsTooLongTitle() {
        Job job = job();
        job.setTitle("岗".repeat(101));

        assertEquals("岗位名称不能超过 100 字", jobService.create(job));
        verify(jobMapper, never()).insert(any());
    }

    @Test
    @DisplayName("发布岗位：招聘人数越界时拒绝")
    void createRejectsOutOfRangeHeadcount() {
        Job job = job();
        job.setHeadcount(0);

        assertEquals("招聘人数需为 1-9999 之间的整数", jobService.create(job));
        verify(jobMapper, never()).insert(any());
    }

    @Test
    @DisplayName("发布岗位：非法状态被拒绝")
    void createRejectsUnknownStatus() {
        Job job = job();
        job.setStatus("草稿");

        assertEquals("岗位状态不合法", jobService.create(job));
        verify(jobMapper, never()).insert(any());
    }

    @Test
    @DisplayName("发布岗位：薪资超过 50 字时拒绝")
    void createRejectsTooLongSalary() {
        Job job = job();
        job.setSalary("薪".repeat(51));

        assertEquals("薪资待遇不能超过 50 字", jobService.create(job));
        verify(jobMapper, never()).insert(any());
    }

    @Test
    @DisplayName("编辑岗位：岗位不存在时提示且不写库")
    void updateRejectsMissingJob() {
        Job job = job();
        job.setId(9L);
        when(jobMapper.findById(9L)).thenReturn(null);

        assertEquals("岗位不存在或已被删除", jobService.update(job));
        verify(jobMapper, never()).update(any());
    }

    @Test
    @DisplayName("编辑岗位：校验通过后写库")
    void updateSavesJob() {
        Job job = job();
        job.setId(1L);
        when(jobMapper.findById(1L)).thenReturn(job());

        assertNull(jobService.update(job));
        verify(jobMapper).update(job);
    }

    @Test
    @DisplayName("上架岗位：状态改回招聘中并写库")
    void onlineReopensJob() {
        Job job = job();
        job.setId(1L);
        job.setStatus("已截止");
        when(jobMapper.findById(1L)).thenReturn(job);

        jobService.online(1L);

        assertEquals("招聘中", job.getStatus());
        verify(jobMapper).update(job);
    }
}

package com.employment.service;

import com.employment.entity.Job;
import com.employment.entity.JobApplication;
import com.employment.mapper.JobApplicationMapper;
import com.employment.mapper.JobMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 申请审核业务单元测试：验证状态校验、数据库写入与审核日志。
 */
@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    private static final Long ID = 1L;
    private static final Long ADMIN_ID = 9L;
    private static final String ADMIN_NAME = "李越";

    @Mock
    private JobApplicationMapper applicationMapper;

    @Mock
    private JobMapper jobMapper;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ApplicationService applicationService;

    private static final Long JOB_ID = 3L;
    private static final Long STUDENT_ID = 5L;

    private Job job(String status) {
        Job job = new Job();
        job.setId(JOB_ID);
        job.setTitle("Java 开发工程师");
        job.setStatus(status);
        return job;
    }

    private JobApplication application(String status, String interviewResult, String hireStatus) {
        JobApplication a = new JobApplication();
        a.setId(ID);
        a.setStatus(status);
        a.setInterviewResult(interviewResult);
        a.setHireStatus(hireStatus);
        return a;
    }

    @Test
    @DisplayName("初审通过：写库并记录审核日志")
    void approveSuccess() {
        when(applicationMapper.findById(ID)).thenReturn(application("待审核", null, null));

        String error = applicationService.approve(ID, "专业对口", ADMIN_ID, ADMIN_NAME);

        assertNull(error);
        verify(applicationMapper).updateReview(eq(ID), eq("通过"), ArgumentMatchers.<String>isNull(),
                eq(ADMIN_ID), eq(ADMIN_NAME), any(LocalDateTime.class));
        verify(auditService).record(eq(AuditService.TYPE_APPLICATION), eq(ID), eq("APPROVE"),
                anyString(), anyString(), anyString(), eq(ADMIN_ID), eq(ADMIN_NAME), eq("专业对口"));
    }

    @Test
    @DisplayName("初审通过：已完成初审的申请被拦截，不写库")
    void approveBlocked() {
        when(applicationMapper.findById(ID)).thenReturn(application("通过", "未安排", "待定"));

        String error = applicationService.approve(ID, null, ADMIN_ID, ADMIN_NAME);

        assertNotNull(error);
        verify(applicationMapper, never()).updateReview(any(), any(), any(), any(), any(), any());
        verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("初审不通过：未填写原因时直接拒绝，不查库")
    void rejectRequiresReason() {
        String error = applicationService.reject(ID, "   ", ADMIN_ID, ADMIN_NAME);

        assertEquals("初审不通过时必须填写原因", error);
        verifyNoInteractions(applicationMapper, auditService);
    }

    @Test
    @DisplayName("安排面试：未填面试时间时拒绝")
    void scheduleRequiresTime() {
        String error = applicationService.schedule(ID, null, "行政楼 302", ADMIN_ID, ADMIN_NAME);

        assertEquals("请填写面试时间", error);
        verifyNoInteractions(applicationMapper, auditService);
    }

    @Test
    @DisplayName("安排面试：未初审通过时拒绝")
    void scheduleRequiresPassed() {
        when(applicationMapper.findById(ID)).thenReturn(application("待审核", null, null));

        String error = applicationService.schedule(ID, LocalDateTime.now().plusDays(1), "线上",
                ADMIN_ID, ADMIN_NAME);

        assertNotNull(error);
        verify(applicationMapper, never()).updateInterview(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("登记录用：面试未通过时拒绝")
    void hireBeforeInterviewPassed() {
        when(applicationMapper.findById(ID)).thenReturn(application("通过", "待面试", "待定"));

        String error = applicationService.hire(ID, true, null, ADMIN_ID, ADMIN_NAME);

        assertNotNull(error);
        verify(applicationMapper, never()).updateHire(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("登记录用：面试通过后成功写入录用状态")
    void hireSuccess() {
        when(applicationMapper.findById(ID)).thenReturn(application("通过", "面试通过", "待定"));

        String error = applicationService.hire(ID, true, "已签三方", ADMIN_ID, ADMIN_NAME);

        assertNull(error);
        verify(applicationMapper).updateHire(eq(ID), eq("已录用"), eq(ADMIN_ID), eq(ADMIN_NAME),
                any(LocalDateTime.class));
        verify(auditService).record(eq(AuditService.TYPE_APPLICATION), eq(ID), eq("HIRE"),
                anyString(), anyString(), anyString(), eq(ADMIN_ID), eq(ADMIN_NAME), eq("已签三方"));
    }

    // ==================== 学生申请岗位 ====================

    @Test
    @DisplayName("申请岗位：岗位不存在时拒绝，不写库")
    void applyRejectsMissingJob() {
        when(jobMapper.findById(JOB_ID)).thenReturn(null);

        assertEquals("岗位不存在或已被删除", applicationService.apply(JOB_ID, STUDENT_ID));
        verifyNoInteractions(applicationMapper, auditService);
    }

    @Test
    @DisplayName("申请岗位：岗位已截止时拒绝，不写库")
    void applyRejectsClosedJob() {
        when(jobMapper.findById(JOB_ID)).thenReturn(job("已截止"));

        String error = applicationService.apply(JOB_ID, STUDENT_ID);

        assertNotNull(error);
        assertTrue(error.contains("已停止招聘"), "提示应说明岗位已停止招聘");
        verify(applicationMapper, never()).insert(any(), any());
        verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("申请岗位：重复申请同一岗位时拒绝")
    void applyRejectsDuplicate() {
        when(jobMapper.findById(JOB_ID)).thenReturn(job("招聘中"));
        when(applicationMapper.findByJobAndStudent(JOB_ID, STUDENT_ID)).thenReturn(new JobApplication());

        assertEquals("您已申请过该岗位，请勿重复申请", applicationService.apply(JOB_ID, STUDENT_ID));
        verify(applicationMapper, never()).insert(any(), any());
        verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("申请岗位：校验通过后写入申请并记录提交日志")
    void applySuccessRecordsAudit() {
        JobApplication created = new JobApplication();
        created.setId(77L);
        when(jobMapper.findById(JOB_ID)).thenReturn(job("招聘中"));
        when(applicationMapper.findByJobAndStudent(JOB_ID, STUDENT_ID)).thenReturn(null, created);

        String error = applicationService.apply(JOB_ID, STUDENT_ID);

        assertNull(error);
        verify(applicationMapper).insert(JOB_ID, STUDENT_ID);
        verify(auditService).record(eq(AuditService.TYPE_APPLICATION), eq(77L), eq("SUBMIT"),
                anyString(), ArgumentMatchers.<String>isNull(), anyString(),
                eq(STUDENT_ID), anyString(), ArgumentMatchers.<String>isNull());
    }

    @Test
    @DisplayName("是否已申请：未登录（studentId 为空）时直接返回 false")
    void hasAppliedWithoutStudent() {
        assertFalse(applicationService.hasApplied(JOB_ID, null));
        verifyNoInteractions(jobMapper, applicationMapper);
    }

    @Test
    @DisplayName("已申请岗位 ID 集合：按学生去重")
    void myAppliedJobIdsDistinct() {
        JobApplication first = new JobApplication();
        first.setJobId(1L);
        JobApplication second = new JobApplication();
        second.setJobId(2L);
        JobApplication duplicate = new JobApplication();
        duplicate.setJobId(1L);
        when(applicationMapper.findByStudent(anyLong())).thenReturn(List.of(first, second, duplicate));

        Set<Long> ids = applicationService.myAppliedJobIds(STUDENT_ID);

        assertEquals(Set.of(1L, 2L), ids);
    }
}

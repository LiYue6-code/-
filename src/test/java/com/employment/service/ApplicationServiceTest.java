package com.employment.service;

import com.employment.entity.JobApplication;
import com.employment.mapper.JobApplicationMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
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
    private AuditService auditService;

    @InjectMocks
    private ApplicationService applicationService;

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
}

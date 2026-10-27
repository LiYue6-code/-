package com.employment.service;

import com.employment.entity.EmploymentInfo;
import com.employment.mapper.EmploymentInfoMapper;
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
 * 就业审核业务单元测试：审核状态校验、退回必须填意见、补正重报条件。
 */
@ExtendWith(MockitoExtension.class)
class EmploymentServiceTest {

    private static final Long ID = 1L;
    private static final Long STUDENT_ID = 5L;

    @Mock
    private EmploymentInfoMapper employmentInfoMapper;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private EmploymentService employmentService;

    private EmploymentInfo info(String status) {
        EmploymentInfo e = new EmploymentInfo();
        e.setId(ID);
        e.setStudentId(STUDENT_ID);
        e.setCompanyName("某信息技术有限公司");
        e.setStatus(status);
        return e;
    }

    @Test
    @DisplayName("确认通过：待审核状态可确认，并记录日志")
    void confirmSuccess() {
        when(employmentInfoMapper.findById(ID)).thenReturn(info("待审核"));

        String error = employmentService.review(ID, true, "材料齐全", 9L, "李越");

        assertNull(error);
        verify(employmentInfoMapper).updateReview(eq(ID), eq("已确认"), eq("材料齐全"),
                eq(9L), eq("李越"), any(LocalDateTime.class));
        verify(auditService).record(eq(AuditService.TYPE_EMPLOYMENT), eq(ID), eq("CONFIRM"),
                anyString(), anyString(), anyString(), eq(9L), eq("李越"), eq("材料齐全"));
    }

    @Test
    @DisplayName("确认通过：已确认的记录不能再确认")
    void confirmTwiceFails() {
        when(employmentInfoMapper.findById(ID)).thenReturn(info("已确认"));

        assertNotNull(employmentService.review(ID, true, null, 9L, "李越"));
        verify(employmentInfoMapper, never()).updateReview(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("退回补正：必须填写补正内容")
    void returnRequiresComment() {
        when(employmentInfoMapper.findById(ID)).thenReturn(info("待审核"));

        assertNotNull(employmentService.review(ID, false, "  ", 9L, "李越"));
        verify(employmentInfoMapper, never()).updateReview(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("学生重新上报：只有已退回状态可以重报")
    void resubmitRequiresReturned() {
        when(employmentInfoMapper.findDetailById(ID)).thenReturn(info("待审核"));

        EmploymentInfo form = new EmploymentInfo();
        form.setCompanyName("新单位");

        assertNotNull(employmentService.resubmit(ID, form, STUDENT_ID, null, null));
        verify(employmentInfoMapper, never()).resubmitKeepFile(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("学生重新上报：已退回时回到待审核并累加重报次数")
    void resubmitSuccess() {
        when(employmentInfoMapper.findDetailById(ID)).thenReturn(info("已退回"));

        EmploymentInfo form = new EmploymentInfo();
        form.setCompanyName("新单位");
        form.setPosition("后端开发");

        String error = employmentService.resubmit(ID, form, STUDENT_ID, null, null);

        assertNull(error);
        verify(employmentInfoMapper).resubmitKeepFile(eq(ID), eq("新单位"), eq("后端开发"),
                ArgumentMatchers.<String>isNull(), any(LocalDateTime.class));
        verify(auditService).record(eq(AuditService.TYPE_EMPLOYMENT), eq(ID), eq("RESUBMIT"),
                anyString(), anyString(), anyString(), eq(STUDENT_ID), anyString(),
                ArgumentMatchers.<String>isNull());
    }

    @Test
    @DisplayName("学生上报：已有待审核记录时不允许重复上报")
    void reportTwiceFails() {
        when(employmentInfoMapper.findActiveByStudent(STUDENT_ID)).thenReturn(info("待审核"));

        EmploymentInfo form = new EmploymentInfo();
        form.setStudentId(STUDENT_ID);
        form.setCompanyName("某公司");

        assertNotNull(employmentService.report(form));
        verify(employmentInfoMapper, never()).insert(any());
    }

    @Test
    @DisplayName("学生上报：就业单位为空时拒绝，且不查库")
    void reportRequiresCompany() {
        EmploymentInfo form = new EmploymentInfo();
        form.setStudentId(STUDENT_ID);
        form.setCompanyName(" ");

        assertNotNull(employmentService.report(form));
        verifyNoInteractions(employmentInfoMapper);
    }

    @Test
    @DisplayName("学生上报：就业单位超过 100 字时拒绝，且不写库")
    void reportRejectsTooLongCompany() {
        EmploymentInfo form = new EmploymentInfo();
        form.setStudentId(STUDENT_ID);
        form.setCompanyName("单".repeat(101));

        assertEquals("就业单位名称不能超过 100 个字符", employmentService.report(form));
        verifyNoInteractions(employmentInfoMapper, auditService);
    }

    @Test
    @DisplayName("学生上报：保存前去除首尾空格，空串转为 null")
    void reportTrimsFields() {
        when(employmentInfoMapper.findActiveByStudent(STUDENT_ID)).thenReturn(null);

        EmploymentInfo form = new EmploymentInfo();
        form.setStudentId(STUDENT_ID);
        form.setCompanyName("  某信息技术有限公司  ");
        form.setPosition("  ");
        form.setSalary(" 8000-12000 ");

        assertNull(employmentService.report(form));

        assertEquals("某信息技术有限公司", form.getCompanyName());
        assertNull(form.getPosition());
        assertEquals("8000-12000", form.getSalary());
        verify(employmentInfoMapper).insert(form);
    }

    @Test
    @DisplayName("补正重报：岗位名称超过 100 字时拒绝，且不写库")
    void resubmitRejectsTooLongPosition() {
        when(employmentInfoMapper.findDetailById(ID)).thenReturn(info("已退回"));

        EmploymentInfo form = new EmploymentInfo();
        form.setCompanyName("某公司");
        form.setPosition("岗".repeat(101));

        assertEquals("岗位名称不能超过 100 个字符",
                employmentService.resubmit(ID, form, STUDENT_ID, null, null));
        verify(employmentInfoMapper, never()).resubmitKeepFile(any(), any(), any(), any(), any());
    }
}

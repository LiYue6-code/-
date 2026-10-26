package com.employment.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.employment.service.ApplicationFlow.Action;
import static com.employment.service.ApplicationFlow.IV_FAIL;
import static com.employment.service.ApplicationFlow.IV_NONE;
import static com.employment.service.ApplicationFlow.IV_PASS;
import static com.employment.service.ApplicationFlow.IV_WAIT;
import static com.employment.service.ApplicationFlow.PASSED;
import static com.employment.service.ApplicationFlow.REJECTED;
import static com.employment.service.ApplicationFlow.WAIT;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 申请状态机单元测试：覆盖每个动作的合法与非法迁移。
 */
class ApplicationFlowTest {

    @Test
    @DisplayName("待审核状态可以初审通过")
    void approveFromWait() {
        assertNull(ApplicationFlow.check(Action.APPROVE, WAIT, null, null));
    }

    @Test
    @DisplayName("已初审的申请不能重复初审")
    void approveTwiceFails() {
        assertNotNull(ApplicationFlow.check(Action.APPROVE, PASSED, null, null));
        assertNotNull(ApplicationFlow.check(Action.APPROVE, REJECTED, null, null));
    }

    @Test
    @DisplayName("未初审通过不能安排面试")
    void scheduleRequiresPassed() {
        assertNotNull(ApplicationFlow.check(Action.SCHEDULE, WAIT, null, null));
        assertNull(ApplicationFlow.check(Action.SCHEDULE, PASSED, null, null));
    }

    @Test
    @DisplayName("面试结果已登记时不能重复安排面试")
    void scheduleAfterResultFails() {
        assertNotNull(ApplicationFlow.check(Action.SCHEDULE, PASSED, IV_PASS, null));
        assertNotNull(ApplicationFlow.check(Action.SCHEDULE, PASSED, IV_FAIL, null));
    }

    @Test
    @DisplayName("只有待面试状态才能登记面试结果")
    void interviewResultRequiresWaiting() {
        assertNotNull(ApplicationFlow.check(Action.INTERVIEW_PASS, PASSED, IV_NONE, null));
        assertNull(ApplicationFlow.check(Action.INTERVIEW_PASS, PASSED, IV_WAIT, null));
        assertNull(ApplicationFlow.check(Action.INTERVIEW_FAIL, PASSED, IV_WAIT, null));
    }

    @Test
    @DisplayName("面试通过后才能登记录用")
    void hireRequiresInterviewPassed() {
        assertNotNull(ApplicationFlow.check(Action.HIRE, PASSED, IV_WAIT, null));
        assertNotNull(ApplicationFlow.check(Action.HIRE, PASSED, IV_FAIL, null));
        assertNull(ApplicationFlow.check(Action.HIRE, PASSED, IV_PASS, null));
    }

    @Test
    @DisplayName("已录用的申请不能重复录用")
    void hireTwiceFails() {
        assertNotNull(ApplicationFlow.check(Action.HIRE, PASSED, IV_PASS, "已录用"));
        assertNotNull(ApplicationFlow.check(Action.NO_HIRE, PASSED, IV_PASS, "已录用"));
    }

    @Test
    @DisplayName("面试结束后可登记未录用")
    void noHireAfterInterview() {
        assertNull(ApplicationFlow.check(Action.NO_HIRE, PASSED, IV_PASS, null));
        assertNull(ApplicationFlow.check(Action.NO_HIRE, PASSED, IV_FAIL, null));
    }

    @Test
    @DisplayName("空状态按默认值处理，不会抛空指针")
    void nullSafe() {
        assertNull(ApplicationFlow.check(Action.APPROVE, null, null, null));
        assertNotNull(ApplicationFlow.check(Action.SCHEDULE, null, null, null));
    }

    @Test
    @DisplayName("状态描述按 初审/面试/录用 三段展示")
    void describe() {
        String text = ApplicationFlow.describe(PASSED, IV_WAIT, null);
        assertTrue(text.contains(PASSED) && text.contains(IV_WAIT));
    }
}

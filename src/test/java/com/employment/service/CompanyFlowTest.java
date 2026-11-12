package com.employment.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 单位资质审核状态机单元测试。
 */
class CompanyFlowTest {

    @Test
    @DisplayName("资质审核：仅待审核状态可审核")
    void reviewOnlyOnWait() {
        assertNull(CompanyFlow.checkReview(CompanyFlow.REVIEW_WAIT));
        assertNotNull(CompanyFlow.checkReview(CompanyFlow.REVIEW_PASSED));
        assertNotNull(CompanyFlow.checkReview(CompanyFlow.REVIEW_REJECTED));
    }
}
package com.employment.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 岗位审核与上下架状态机单元测试。
 */
class JobFlowTest {

    @Test
    @DisplayName("岗位审核：仅待审核状态可审核")
    void reviewOnlyOnWait() {
        assertNull(JobFlow.checkReview(JobFlow.REVIEW_WAIT));
        assertNotNull(JobFlow.checkReview(JobFlow.REVIEW_PASSED));
        assertNotNull(JobFlow.checkReview(JobFlow.REVIEW_REJECTED));
    }

    @Test
    @DisplayName("手动上架：需审核通过且当前已截止")
    void onlineRequiresPassedAndOffline() {
        assertNull(JobFlow.checkOnline(JobFlow.REVIEW_PASSED, JobFlow.OFFLINE));
        assertNotNull(JobFlow.checkOnline(JobFlow.REVIEW_WAIT, JobFlow.OFFLINE));
        assertNotNull(JobFlow.checkOnline(JobFlow.REVIEW_PASSED, JobFlow.ONLINE));
    }

    @Test
    @DisplayName("手动下架：仅招聘中可下架")
    void offlineOnlyOnOnline() {
        assertNull(JobFlow.checkOffline(JobFlow.ONLINE));
        assertNotNull(JobFlow.checkOffline(JobFlow.OFFLINE));
    }
}
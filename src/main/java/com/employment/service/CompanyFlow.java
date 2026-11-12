package com.employment.service;

/**
 * 单位资质审核状态机：待审核 → 通过 / 驳回。
 */
public final class CompanyFlow {

    public static final String REVIEW_WAIT = "待审核";
    public static final String REVIEW_PASSED = "通过";
    public static final String REVIEW_REJECTED = "驳回";

    /** 信用状态 */
    public static final String CREDIT_NORMAL = "正常";
    public static final String CREDIT_ABNORMAL = "异常";

    private CompanyFlow() {
    }

    /** 资质审核（通过 / 驳回）：仅「待审核」可操作。 */
    public static String checkReview(String reviewStatus) {
        String rs = reviewStatus == null ? REVIEW_WAIT : reviewStatus;
        return REVIEW_WAIT.equals(rs) ? null : "该单位资质已审核（当前「" + rs + "」），无需重复审核";
    }
}
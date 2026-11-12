package com.employment.service;

/**
 * 岗位审核与上下架状态机。
 *
 * <p>岗位有两个维度：
 * <ul>
 *     <li>reviewStatus：待审核 → 通过 / 驳回（岗位审核）</li>
 *     <li>status：招聘中（上架）/ 已截止（下架），沿用原字段与中文值</li>
 * </ul>
 * 该类为纯逻辑，不依赖数据库，便于单元测试。
 */
public final class JobFlow {

    /** 审核状态 */
    public static final String REVIEW_WAIT = "待审核";
    public static final String REVIEW_PASSED = "通过";
    public static final String REVIEW_REJECTED = "驳回";

    /** 上架状态（复用 job.status 原中文值） */
    public static final String ONLINE = "招聘中";
    public static final String OFFLINE = "已截止";

    private JobFlow() {
    }

    /** 岗位审核（通过 / 驳回）：仅「待审核」可操作。 */
    public static String checkReview(String reviewStatus) {
        String rs = reviewStatus == null ? REVIEW_WAIT : reviewStatus;
        return REVIEW_WAIT.equals(rs) ? null : "只有「待审核」的岗位可以审核（当前「" + rs + "」）";
    }

    /** 手动上架：审核通过且当前已截止才可上架。 */
    public static String checkOnline(String reviewStatus, String status) {
        if (!REVIEW_PASSED.equals(reviewStatus)) {
            return "岗位未通过审核，不能上架（当前审核「" + reviewStatus + "」）";
        }
        if (!OFFLINE.equals(status)) {
            return "岗位已是「" + status + "」状态，无需上架";
        }
        return null;
    }

    /** 手动下架：仅「招聘中」可下架。 */
    public static String checkOffline(String status) {
        return ONLINE.equals(status) ? null : "岗位已是「" + status + "」状态，无需下架";
    }
}
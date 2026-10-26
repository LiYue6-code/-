package com.employment.service;

/**
 * 就业信息上报状态机：待审核 → 已确认 / 已退回；已退回 → 待审核（学生修改后重新上报）。
 */
public final class EmploymentFlow {

    public static final String WAIT = "待审核";
    public static final String CONFIRMED = "已确认";
    public static final String RETURNED = "已退回";

    private EmploymentFlow() {
    }

    /** 确认通过：仅待审核可操作。 */
    public static String checkConfirm(String status) {
        String st = status == null ? WAIT : status;
        return WAIT.equals(st) ? null : "只有「待审核」的上报可以确认通过（当前「" + st + "」）";
    }

    /** 退回补正：仅待审核可操作。 */
    public static String checkReturn(String status) {
        String st = status == null ? WAIT : status;
        return WAIT.equals(st) ? null : "只有「待审核」的上报可以退回（当前「" + st + "」）";
    }

    /** 学生重新上报：仅已退回可操作。 */
    public static String checkResubmit(String status) {
        return RETURNED.equals(status) ? null : "只有「已退回」的上报可以重新提交（当前「" + status + "」）";
    }
}

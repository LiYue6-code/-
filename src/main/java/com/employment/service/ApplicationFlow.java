package com.employment.service;

/**
 * 岗位申请状态机。
 *
 * <p>申请状态三组字段，任意时刻只有合法组合能通过校验：
 * <ul>
 *     <li>status：待审核 → 通过 / 未通过（初审）</li>
 *     <li>interviewResult：未安排 → 待面试 → 面试通过 / 面试未通过</li>
 *     <li>hireStatus：待定 → 已录用 / 未录用</li>
 * </ul>
 * 该类为纯逻辑，不依赖数据库，便于单元测试。
 */
public final class ApplicationFlow {

    /** 初审状态 */
    public static final String WAIT = "待审核";
    public static final String PASSED = "通过";
    public static final String REJECTED = "未通过";

    /** 面试结果 */
    public static final String IV_NONE = "未安排";
    public static final String IV_WAIT = "待面试";
    public static final String IV_PASS = "面试通过";
    public static final String IV_FAIL = "面试未通过";

    /** 录用状态 */
    public static final String HIRE_WAIT = "待定";
    public static final String HIRED = "已录用";
    public static final String NOT_HIRED = "未录用";

    public static final String ALL_STATUS = WAIT;
    public static final String ALL_PASSED = PASSED;

    private ApplicationFlow() {
    }

    /** 审核动作。 */
    public enum Action {
        APPROVE("初审通过"),
        REJECT("初审不通过"),
        SCHEDULE("安排面试"),
        INTERVIEW_PASS("面试通过"),
        INTERVIEW_FAIL("面试未通过"),
        HIRE("登记录用"),
        NO_HIRE("登记未录用");

        private final String label;

        Action(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * 校验动作是否合法。
     *
     * @return 合法返回 null；不合法返回面向用户的中文提示
     */
    public static String check(Action action, String status, String interviewResult, String hireStatus) {
        String st = defaultIfBlank(status, WAIT);
        String iv = defaultIfBlank(interviewResult, IV_NONE);
        String hs = defaultIfBlank(hireStatus, HIRE_WAIT);

        return switch (action) {
            case APPROVE, REJECT ->
                    WAIT.equals(st) ? null : "当前申请为「" + st + "」状态，只有「待审核」的申请可以初审";
            case SCHEDULE -> {
                if (!PASSED.equals(st)) {
                    yield "初审通过后才能安排面试（当前「" + st + "」）";
                }
                if (IV_PASS.equals(iv) || IV_FAIL.equals(iv)) {
                    yield "面试结果已登记，不能重复安排面试";
                }
                yield null;
            }
            case INTERVIEW_PASS, INTERVIEW_FAIL ->
                    IV_WAIT.equals(iv) ? null : "请先安排面试，再登记面试结果（当前「" + iv + "」）";
            case HIRE -> {
                if (!IV_PASS.equals(iv)) {
                    yield "面试通过后才能登记录用结果（当前面试「" + iv + "」）";
                }
                if (HIRED.equals(hs)) {
                    yield "该申请已登记录用，不能重复登记";
                }
                yield null;
            }
            case NO_HIRE -> {
                if (!IV_PASS.equals(iv) && !IV_FAIL.equals(iv)) {
                    yield "面试结果登记后才能登记未录用（当前面试「" + iv + "」）";
                }
                if (HIRED.equals(hs)) {
                    yield "该申请已登记录用，不能改为未录用";
                }
                yield null;
            }
        };
    }

    /** 生成状态描述，用于审核日志的 from/to 展示。 */
    public static String describe(String status, String interviewResult, String hireStatus) {
        String st = defaultIfBlank(status, WAIT);
        String iv = defaultIfBlank(interviewResult, IV_NONE);
        String hs = defaultIfBlank(hireStatus, HIRE_WAIT);
        return st + " / " + iv + " / " + hs;
    }

    public static String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}

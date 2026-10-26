package com.employment.service;

/**
 * 登录结果：success 是否成功，message 失败提示，principal 成功时的登录主体（Student 或 Admin）。
 */
public class LoginResult {

    private final boolean success;
    private final String message;
    private final Object principal;

    private LoginResult(boolean success, String message, Object principal) {
        this.success = success;
        this.message = message;
        this.principal = principal;
    }

    public static LoginResult success(Object principal) {
        return new LoginResult(true, null, principal);
    }

    public static LoginResult failure(String message) {
        return new LoginResult(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getPrincipal() {
        return principal;
    }
}

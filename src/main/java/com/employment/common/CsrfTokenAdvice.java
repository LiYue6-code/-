package com.employment.common;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.UUID;

/**
 * 为所有视图暴露 CSRF token：token 存于会话，渲染为表单隐藏域的值。
 */
@ControllerAdvice
public class CsrfTokenAdvice {

    public static final String SESSION_ATTR = "csrf_token";
    public static final String PARAM_NAME = "_csrf";

    /** 每个页面渲染前确保会话中存在 token，并作为模型属性 "_csrf" 提供给模板。 */
    @ModelAttribute("_csrf")
    public String csrfToken(HttpSession session) {
        Object token = session.getAttribute(SESSION_ATTR);
        if (token == null) {
            token = UUID.randomUUID().toString();
            session.setAttribute(SESSION_ATTR, token);
        }
        return (String) token;
    }
}

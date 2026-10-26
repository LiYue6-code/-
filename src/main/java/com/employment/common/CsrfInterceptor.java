package com.employment.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * CSRF 校验拦截器：对所有 POST 请求校验隐藏域 token 与会话 token 是否一致。
 */
@Component
public class CsrfInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        HttpSession session = request.getSession(false);
        String expected = session == null ? null : (String) session.getAttribute(CsrfTokenAdvice.SESSION_ATTR);
        String actual = request.getParameter(CsrfTokenAdvice.PARAM_NAME);
        if (expected == null || !expected.equals(actual)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token 校验失败");
            return false;
        }
        return true;
    }
}

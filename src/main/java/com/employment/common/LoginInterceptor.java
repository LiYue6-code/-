package com.employment.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器：未登录跳转到登录页，/admin 接口仅管理员可访问。
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        Object userType = session == null ? null : session.getAttribute("userType");
        String contextPath = request.getContextPath();

        if (userType == null) {
            response.sendRedirect(contextPath + "/login");
            return false;
        }
        // 管理员接口权限校验
        if (request.getRequestURI().startsWith(contextPath + "/admin") && !"admin".equals(userType)) {
            response.sendRedirect(contextPath + "/login");
            return false;
        }
        return true;
    }
}

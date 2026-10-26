package com.employment.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录与角色拦截器：未登录跳转登录页；/admin 仅管理员可访问；学生专属路径仅学生可访问。
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

        String uri = request.getRequestURI();

        // 管理员专属路径
        if (uri.startsWith(contextPath + "/admin") && !"admin".equals(userType)) {
            response.sendRedirect(contextPath + "/home");
            return false;
        }
        // 学生专属路径
        if (isStudentOnly(uri, contextPath) && !"student".equals(userType)) {
            response.sendRedirect(contextPath + "/home");
            return false;
        }
        return true;
    }

    private boolean isStudentOnly(String uri, String contextPath) {
        return uri.startsWith(contextPath + "/my")
                || uri.startsWith(contextPath + "/profile")
                || uri.startsWith(contextPath + "/jobs");
    }
}

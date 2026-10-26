package com.employment.config;

import com.employment.common.CsrfInterceptor;
import com.employment.common.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置：注册 CSRF 校验拦截器与登录拦截器。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;
    private final CsrfInterceptor csrfInterceptor;

    public WebConfig(LoginInterceptor loginInterceptor, CsrfInterceptor csrfInterceptor) {
        this.loginInterceptor = loginInterceptor;
        this.csrfInterceptor = csrfInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // CSRF 校验：对所有 POST 请求生效（含登录/注册/退出）
        registry.addInterceptor(csrfInterceptor).addPathPatterns("/**");
        // 登录校验
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/register", "/logout",
                        "/css/**", "/js/**", "/images/**", "/error");
    }
}

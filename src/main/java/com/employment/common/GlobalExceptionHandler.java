package com.employment.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理：把业务异常转换为友好的错误页。
 *
 * <p>注意：这里不拦截 {@code Exception}，否则会把 Spring MVC 自身的 405/415 等
 * 语义异常统一变成 500。未预期异常交给 Spring Boot 默认错误处理（渲染 error.html）。
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务异常：展示提示信息。 */
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBiz(BizException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error";
    }

    /** 缺少必要参数或参数类型不匹配：400，提示重新填写。 */
    @ExceptionHandler({MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(Exception e, Model model) {
        model.addAttribute("message", "提交的参数不完整或格式不正确，请返回修改后重试。");
        return "error";
    }

    /** 静态资源/不存在的路径：404。 */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(Model model) {
        model.addAttribute("message", "访问的页面或资源不存在。");
        return "error";
    }
}

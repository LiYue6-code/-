package com.employment.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首页：按角色跳转到对应功能页。
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home(HttpSession session) {
        if ("admin".equals(session.getAttribute("userType"))) {
            return "redirect:/admin/statistics";
        }
        return "redirect:/jobs";
    }
}

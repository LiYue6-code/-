package com.employment.controller;

import com.employment.service.MessageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 站内信控制器：收件箱、单条已读、全部已读（学生与负责人共用）。
 */
@Controller
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/messages")
    public String inbox(HttpSession session, Model model) {
        String type = (String) session.getAttribute("userType");
        Long id = (Long) session.getAttribute("userId");
        model.addAttribute("messages", messageService.list(type, id));
        return "messages";
    }

    @PostMapping("/messages/{id}/read")
    public String read(@PathVariable Long id, HttpSession session) {
        String type = (String) session.getAttribute("userType");
        Long userId = (Long) session.getAttribute("userId");
        messageService.markRead(id, type, userId);
        return "redirect:/messages";
    }

    @PostMapping("/messages/read-all")
    public String readAll(HttpSession session) {
        String type = (String) session.getAttribute("userType");
        Long id = (Long) session.getAttribute("userId");
        messageService.markAllRead(type, id);
        return "redirect:/messages";
    }
}
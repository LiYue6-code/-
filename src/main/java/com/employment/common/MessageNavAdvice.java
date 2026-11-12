package com.employment.common;

import com.employment.service.MessageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 向所有视图暴露当前用户未读站内信数量，供顶部导航角标使用。
 */
@ControllerAdvice
public class MessageNavAdvice {

    private final MessageService messageService;

    public MessageNavAdvice(MessageService messageService) {
        this.messageService = messageService;
    }

    @ModelAttribute("unreadMessages")
    public long unreadCount(HttpSession session) {
        String type = (String) session.getAttribute("userType");
        Long id = (Long) session.getAttribute("userId");
        if (type == null || id == null) {
            return 0;
        }
        return messageService.unreadCount(type, id);
    }
}
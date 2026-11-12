package com.employment.service;

import com.employment.entity.Admin;
import com.employment.entity.Message;
import com.employment.mapper.AdminMapper;
import com.employment.mapper.MessageMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 站内消息业务：向学生或负责人发送通知，并提供收件箱与已读管理。
 */
@Service
public class MessageService {

    public static final String TYPE_STUDENT = "student";
    public static final String TYPE_ADMIN = "admin";

    private final MessageMapper messageMapper;
    private final AdminMapper adminMapper;

    public MessageService(MessageMapper messageMapper, AdminMapper adminMapper) {
        this.messageMapper = messageMapper;
        this.adminMapper = adminMapper;
    }

    public List<Message> list(String receiverType, Long receiverId) {
        return messageMapper.findByReceiver(receiverType, receiverId);
    }

    public long unreadCount(String receiverType, Long receiverId) {
        return messageMapper.countUnread(receiverType, receiverId);
    }

    /** 给学生发送站内信。 */
    public void sendToStudent(Long studentId, String title, String content) {
        if (studentId == null) {
            return;
        }
        insert(TYPE_STUDENT, studentId, title, content);
    }

    /** 给所有负责人发送站内信（用于待办提醒）。 */
    public void sendToAdmins(String title, String content) {
        for (Admin admin : adminMapper.findAll()) {
            insert(TYPE_ADMIN, admin.getId(), title, content);
        }
    }

    public void markRead(Long id, String receiverType, Long receiverId) {
        messageMapper.markRead(id, receiverType, receiverId);
    }

    public void markAllRead(String receiverType, Long receiverId) {
        messageMapper.markAllRead(receiverType, receiverId);
    }

    private void insert(String receiverType, Long receiverId, String title, String content) {
        Message message = new Message();
        message.setReceiverType(receiverType);
        message.setReceiverId(receiverId);
        message.setTitle(title);
        message.setContent(content);
        messageMapper.insert(message);
    }
}
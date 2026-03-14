package com.tutoringsys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutoringsys.dao.entity.Message;
import com.tutoringsys.dao.entity.UserMessage;

import java.util.List;
import java.util.Map;

public interface MessageService {
    // 发送消息给指定用户
    void sendMessage(Long userId, String title, String content, String type, Long relatedId, String relatedType);
    
    // 发送消息给多个用户
    void sendMessageToUsers(List<Long> userIds, String title, String content, String type, Long relatedId, String relatedType);
    
    // 获取用户消息列表
    IPage<Map<String, Object>> getUserMessages(Long userId, int page, int size, String type);
    
    // 标记消息为已读
    void markAsRead(Long userId, Long messageId);
    
    // 标记所有消息为已读
    void markAllAsRead(Long userId);
    
    // 获取未读消息数量
    int getUnreadCount(Long userId);
}

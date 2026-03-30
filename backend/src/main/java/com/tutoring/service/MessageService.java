package com.tutoring.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tutoring.dto.MessageVO;
import com.tutoring.entity.Message;

import java.util.List;

public interface MessageService extends IService<Message> {

    void sendMessage(Message message, List<Long> receiverIds);

    Page<MessageVO> getUserMessages(Long userId, Integer page, Integer size, String type, String keyword, Integer readStatus);

    Long getUnreadCount(Long userId);

    void markAsRead(Long userId, Long messageId);

    void markAllAsRead(Long userId);

    MessageVO getMessageDetail(Long userId, Long messageId);

}

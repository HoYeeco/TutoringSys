package com.tutoringsys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutoringsys.dao.entity.Message;
import com.tutoringsys.dao.entity.UserMessage;
import com.tutoringsys.dao.mapper.MessageMapper;
import com.tutoringsys.dao.mapper.UserMessageMapper;
import com.tutoringsys.service.MessageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private UserMessageMapper userMessageMapper;

    @Override
    public void sendMessage(Long userId, String title, String content, String type, Long relatedId, String relatedType) {
        // 创建消息
        Message message = new Message();
        message.setTitle(title);
        message.setContent(content);
        message.setType(type);
        message.setRelatedId(relatedId);
        message.setRelatedType(relatedType);
        messageMapper.insert(message);

        // 创建用户消息关联
        UserMessage userMessage = new UserMessage();
        userMessage.setUserId(userId);
        userMessage.setMessageId(message.getId());
        userMessage.setIsRead(0);
        userMessageMapper.insert(userMessage);
    }

    @Override
    public void sendMessageToUsers(List<Long> userIds, String title, String content, String type, Long relatedId, String relatedType) {
        // 创建消息
        Message message = new Message();
        message.setTitle(title);
        message.setContent(content);
        message.setType(type);
        message.setRelatedId(relatedId);
        message.setRelatedType(relatedType);
        messageMapper.insert(message);

        // 创建用户消息关联
        List<UserMessage> userMessages = userIds.stream().map(userId -> {
            UserMessage userMessage = new UserMessage();
            userMessage.setUserId(userId);
            userMessage.setMessageId(message.getId());
            userMessage.setIsRead(0);
            return userMessage;
        }).collect(Collectors.toList());

        // 批量插入用户消息关联
        for (UserMessage userMessage : userMessages) {
            userMessageMapper.insert(userMessage);
        }
    }

    @Override
    public IPage<Map<String, Object>> getUserMessages(Long userId, int page, int size, String type) {
        // 查询用户消息关联
        LambdaQueryWrapper<UserMessage> userMessageQueryWrapper = new LambdaQueryWrapper<>();
        userMessageQueryWrapper.eq(UserMessage::getUserId, userId);

        // 构建分页查询
        Page<UserMessage> userMessagePage = new Page<>(page, size);
        userMessageMapper.selectPage(userMessagePage, userMessageQueryWrapper);

        // 获取消息ID列表
        List<Long> messageIds = userMessagePage.getRecords().stream()
                .map(UserMessage::getMessageId)
                .collect(Collectors.toList());

        // 查询消息详情
        List<Message> messages = new ArrayList<>();
        if (!messageIds.isEmpty()) {
            LambdaQueryWrapper<Message> messageQueryWrapper = new LambdaQueryWrapper<>();
            messageQueryWrapper.in(Message::getId, messageIds);
            if (type != null && !type.isEmpty()) {
                messageQueryWrapper.eq(Message::getType, type);
            }
            messages = messageMapper.selectList(messageQueryWrapper);
        }

        // 构建消息ID到消息的映射
        Map<Long, Message> messageMap = messages.stream()
                .collect(Collectors.toMap(Message::getId, message -> message));

        // 构建结果
        List<Map<String, Object>> resultList = userMessagePage.getRecords().stream().map(userMessage -> {
            Message message = messageMap.get(userMessage.getMessageId());
            if (message == null) {
                return null;
            }
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", message.getId());
            map.put("title", message.getTitle());
            map.put("content", message.getContent());
            map.put("type", message.getType());
            map.put("relatedId", message.getRelatedId());
            map.put("relatedType", message.getRelatedType());
            map.put("createTime", message.getCreateTime());
            map.put("isRead", userMessage.getIsRead());
            map.put("readTime", userMessage.getReadTime());
            return map;
        }).filter(java.util.Objects::nonNull).collect(Collectors.toList());

        // 构建分页结果
        Page<Map<String, Object>> resultPage = new Page<>(page, size);
        resultPage.setTotal(userMessagePage.getTotal());
        resultPage.setRecords(resultList);

        return resultPage;
    }

    @Override
    public void markAsRead(Long userId, Long messageId) {
        LambdaQueryWrapper<UserMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserMessage::getUserId, userId);
        queryWrapper.eq(UserMessage::getMessageId, messageId);

        UserMessage userMessage = userMessageMapper.selectOne(queryWrapper);
        if (userMessage != null) {
            userMessage.setIsRead(1);
            userMessage.setReadTime(LocalDateTime.now());
            userMessageMapper.updateById(userMessage);
        }
    }

    @Override
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<UserMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserMessage::getUserId, userId);
        queryWrapper.eq(UserMessage::getIsRead, 0);

        List<UserMessage> userMessages = userMessageMapper.selectList(queryWrapper);
        userMessages.forEach(userMessage -> {
            userMessage.setIsRead(1);
            userMessage.setReadTime(LocalDateTime.now());
        });

        if (!userMessages.isEmpty()) {
            // 批量更新用户消息状态
            for (UserMessage userMessage : userMessages) {
                userMessageMapper.updateById(userMessage);
            }
        }
    }

    @Override
    public int getUnreadCount(Long userId) {
        LambdaQueryWrapper<UserMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserMessage::getUserId, userId);
        queryWrapper.eq(UserMessage::getIsRead, 0);

        return userMessageMapper.selectCount(queryWrapper).intValue();
    }
}

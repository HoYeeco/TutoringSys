package com.tutoring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tutoring.dto.MessageVO;
import com.tutoring.entity.Message;
import com.tutoring.entity.UserMessage;
import com.tutoring.mapper.MessageMapper;
import com.tutoring.mapper.UserMessageMapper;
import com.tutoring.service.MessageService;
import com.tutoring.service.UserMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private final UserMessageService userMessageService;
    private final UserMessageMapper userMessageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(Message message, List<Long> receiverIds) {
        save(message);

        List<UserMessage> userMessages = receiverIds.stream()
            .map(userId -> {
                UserMessage um = new UserMessage();
                um.setUserId(userId);
                um.setMessageId(message.getId());
                um.setIsRead(0);
                return um;
            })
            .collect(Collectors.toList());

        userMessageService.saveBatch(userMessages);
    }

    @Override
    public Page<MessageVO> getUserMessages(Long userId, Integer page, Integer size, String type, String keyword, Integer readStatus) {
        LambdaQueryWrapper<UserMessage> queryWrapper = new LambdaQueryWrapper<UserMessage>()
            .eq(UserMessage::getUserId, userId);
        
        // 添加已读状态筛选
        if (readStatus != null) {
            queryWrapper.eq(UserMessage::getIsRead, readStatus);
        }
        
        queryWrapper.orderByDesc(UserMessage::getCreateTime);
        
        Page<UserMessage> umPage = userMessageMapper.selectPage(
            new Page<>(page, size),
            queryWrapper
        );

        List<MessageVO> voList = umPage.getRecords().stream()
            .map(um -> {
                Message msg = getById(um.getMessageId());
                if (msg == null) return null;
                
                if (StringUtils.hasText(type) && !type.equals(msg.getType())) {
                    return null;
                }
                
                if (StringUtils.hasText(keyword)) {
                    if (!msg.getTitle().contains(keyword) && !msg.getContent().contains(keyword)) {
                        return null;
                    }
                }

                return MessageVO.builder()
                    .id(um.getId())
                    .messageId(msg.getId())
                    .title(msg.getTitle())
                    .content(msg.getContent())
                    .type(msg.getType())
                    .relatedId(msg.getRelatedId())
                    .relatedType(msg.getRelatedType())
                    .isRead(um.getIsRead())
                    .readTime(um.getReadTime())
                    .createTime(msg.getCreateTime())
                    .build();
            })
            .filter(vo -> vo != null)
            .collect(Collectors.toList());

        Page<MessageVO> resultPage = new Page<>(page, size, umPage.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return userMessageMapper.selectCount(
            new LambdaQueryWrapper<UserMessage>()
                .eq(UserMessage::getUserId, userId)
                .eq(UserMessage::getIsRead, 0)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long userId, Long messageId) {
        userMessageMapper.update(null,
            new LambdaUpdateWrapper<UserMessage>()
                .set(UserMessage::getIsRead, 1)
                .set(UserMessage::getReadTime, LocalDateTime.now())
                .eq(UserMessage::getUserId, userId)
                .eq(UserMessage::getMessageId, messageId)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        userMessageMapper.update(null,
            new LambdaUpdateWrapper<UserMessage>()
                .set(UserMessage::getIsRead, 1)
                .set(UserMessage::getReadTime, LocalDateTime.now())
                .eq(UserMessage::getUserId, userId)
                .eq(UserMessage::getIsRead, 0)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO getMessageDetail(Long userId, Long messageId) {
        UserMessage um = userMessageMapper.selectOne(
            new LambdaQueryWrapper<UserMessage>()
                .eq(UserMessage::getUserId, userId)
                .eq(UserMessage::getMessageId, messageId)
        );

        if (um == null) {
            return null;
        }

        Message msg = getById(messageId);
        if (msg == null) {
            return null;
        }

        if (um.getIsRead() == 0) {
            markAsRead(userId, messageId);
        }

        return MessageVO.builder()
            .id(um.getId())
            .messageId(msg.getId())
            .title(msg.getTitle())
            .content(msg.getContent())
            .type(msg.getType())
            .relatedId(msg.getRelatedId())
            .relatedType(msg.getRelatedType())
            .isRead(1)
            .readTime(LocalDateTime.now())
            .createTime(msg.getCreateTime())
            .build();
    }

}

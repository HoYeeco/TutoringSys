package com.tutoring.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.common.Result;
import com.tutoring.dto.MessageVO;
import com.tutoring.entity.User;
import com.tutoring.service.MessageService;
import com.tutoring.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "消息管理", description = "消息相关接口")
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    @Operation(summary = "获取消息列表")
    @GetMapping("/list")
    public Result<Page<MessageVO>> getMessageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {
        
        Long userId = getCurrentUserId();
        Page<MessageVO> messagePage = messageService.getUserMessages(userId, page, size, type, keyword);
        return Result.success(messagePage);
    }

    @Operation(summary = "获取未读消息数量")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        Long userId = getCurrentUserId();
        Long count = messageService.getUnreadCount(userId);
        return Result.success(count);
    }

    @Operation(summary = "标记消息已读")
    @PutMapping("/read/{messageId}")
    public Result<String> markAsRead(@PathVariable Long messageId) {
        Long userId = getCurrentUserId();
        messageService.markAsRead(userId, messageId);
        return Result.success("标记成功");
    }

    @Operation(summary = "一键已读")
    @PutMapping("/read-all")
    public Result<String> markAllAsRead() {
        Long userId = getCurrentUserId();
        messageService.markAllAsRead(userId);
        return Result.success("全部标记已读成功");
    }

    @Operation(summary = "获取消息详情")
    @GetMapping("/detail/{messageId}")
    public Result<MessageVO> getMessageDetail(@PathVariable Long messageId) {
        Long userId = getCurrentUserId();
        MessageVO message = messageService.getMessageDetail(userId, messageId);
        if (message == null) {
            return Result.error(404, "消息不存在");
        }
        return Result.success(message);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未认证");
        }
        String username = authentication.getName();
        User user = userService.lambdaQuery()
            .eq(User::getUsername, username)
            .one();
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user.getId();
    }

}

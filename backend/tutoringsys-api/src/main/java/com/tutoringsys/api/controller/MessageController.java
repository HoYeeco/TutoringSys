package com.tutoringsys.api.controller;

import com.tutoringsys.common.response.R;
import com.tutoringsys.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/message")
@Tag(name = "消息管理", description = "消息相关接口")
public class MessageController {

    @Resource
    private MessageService messageService;

    @GetMapping("/list")
    @Operation(summary = "获取消息列表", description = "获取用户消息列表")
    public R<?> getMessageList(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type) {
        try {
            var messages = messageService.getUserMessages(userId, page, size, type);
            return R.ok(messages);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error(500, "获取消息列表失败");
        }
    }

    @PutMapping("/read/{messageId}")
    @Operation(summary = "标记消息为已读", description = "标记指定消息为已读")
    public R<?> markAsRead(
            @PathVariable Long messageId,
            @RequestParam Long userId) {
        try {
            messageService.markAsRead(userId, messageId);
            return R.ok("标记成功");
        } catch (Exception e) {
            e.printStackTrace();
            return R.error(500, "标记失败");
        }
    }

    @PutMapping("/read/all")
    @Operation(summary = "标记所有消息为已读", description = "标记用户所有消息为已读")
    public R<?> markAllAsRead(@RequestParam Long userId) {
        try {
            messageService.markAllAsRead(userId);
            return R.ok("标记成功");
        } catch (Exception e) {
            e.printStackTrace();
            return R.error(500, "标记失败");
        }
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读消息数量", description = "获取用户未读消息数量")
    public R<?> getUnreadCount(@RequestParam Long userId) {
        try {
            int count = messageService.getUnreadCount(userId);
            return R.ok(count);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error(500, "获取未读消息数量失败");
        }
    }
}

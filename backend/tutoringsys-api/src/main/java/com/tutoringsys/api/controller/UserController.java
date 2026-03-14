package com.tutoringsys.api.controller;

import com.tutoringsys.common.response.R;
import com.tutoringsys.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController {

    @Resource
    private UserService userService;

    @PutMapping("/avatar")
    @Operation(summary = "修改头像", description = "修改用户头像")
    public R<?> updateAvatar(@RequestBody AvatarRequest request) {
        try {
            var user = userService.updateAvatar(request.getUserId(), request.getAvatarUrl());
            return R.ok(user.getAvatar());
        } catch (Exception e) {
            e.printStackTrace();
            return R.error(500, "修改头像失败");
        }
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码", description = "修改用户密码")
    public R<?> updatePassword(@RequestBody PasswordRequest request) {
        try {
            boolean success = userService.updatePassword(request.getUserId(), request.getOldPassword(), request.getNewPassword());
            if (success) {
                return R.ok("密码修改成功");
            } else {
                return R.error(400, "旧密码错误");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return R.error(500, "修改密码失败");
        }
    }

    @GetMapping("/login-records")
    @Operation(summary = "获取登录记录", description = "获取用户最近登录记录")
    public R<?> getLoginRecords(@RequestParam Long userId, @RequestParam(defaultValue = "10") int limit) {
        try {
            var records = userService.getLoginRecords(userId, limit);
            return R.ok(records);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error(500, "获取登录记录失败");
        }
    }

    // 头像修改请求DTO
    public static class AvatarRequest {
        private Long userId;
        private String avatarUrl;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    }

    // 密码修改请求DTO
    public static class PasswordRequest {
        private Long userId;
        private String oldPassword;
        private String newPassword;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}

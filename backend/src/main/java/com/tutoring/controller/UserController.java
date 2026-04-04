package com.tutoring.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.common.Result;
import com.tutoring.config.FileProperties;
import com.tutoring.dto.LoginRecordResponse;
import com.tutoring.dto.UpdateAvatarRequest;
import com.tutoring.dto.UpdatePasswordRequest;
import com.tutoring.dto.UserInfoResponse;
import com.tutoring.entity.LoginRecord;
import com.tutoring.entity.User;
import com.tutoring.service.LoginRecordService;
import com.tutoring.service.UserService;
import com.tutoring.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Tag(name = "个人中心", description = "用户个人中心接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final LoginRecordService loginRecordService;
    private final PasswordEncoder passwordEncoder;
    private final FileProperties fileProperties;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/profile")
    public Result<UserInfoResponse> getProfile() {
        String username = SecurityUtil.getCurrentUsername();
        User user = userService.lambdaQuery()
            .eq(User::getUsername, username)
            .one();

        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        UserInfoResponse response = UserInfoResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .realName(user.getRealName())
            .role(user.getRole())
            .avatar(user.getAvatar())
            .email(user.getEmail())
            .phone(user.getPhone())
            .status(user.getStatus())
            .createTime(user.getCreateTime())
            .build();

        return Result.success(response);
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<String> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        String username = SecurityUtil.getCurrentUsername();
        User user = userService.lambdaQuery()
            .eq(User::getUsername, username)
            .one();

        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return Result.error(400, "旧密码错误");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userService.updateById(user);

        return Result.success("密码修改成功");
    }

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }

        String username = SecurityUtil.getCurrentUsername();
        User user = userService.lambdaQuery()
            .eq(User::getUsername, username)
            .one();

        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return Result.error(400, "文件名不能为空");
        }

        String extension = "";
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }
        
        String[] allowedTypes = {"jpg", "jpeg", "png", "gif", "webp", "bmp"};
        boolean isAllowed = false;
        for (String type : allowedTypes) {
            if (type.equals(extension)) {
                isAllowed = true;
                break;
            }
        }
        if (!isAllowed) {
            return Result.error(400, "不支持的文件类型，仅支持: jpg, jpeg, png, gif, webp, bmp");
        }

        long maxSize = 10 * 1024 * 1024L;
        if (file.getSize() > maxSize) {
            return Result.error(400, "文件大小超过限制，最大允许10MB");
        }

        try {
            String uploadDir = fileProperties.getUploadDir();
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String newFilename = UUID.randomUUID().toString() + "." + extension;
            Path filePath = uploadPath.resolve(newFilename);
            file.transferTo(filePath.toFile());

            String avatarUrl = "/uploads/" + newFilename;
            user.setAvatar(avatarUrl);
            userService.updateById(user);

            return Result.success(avatarUrl);
        } catch (IOException e) {
            return Result.error(500, "文件上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新头像URL")
    @PutMapping("/avatar")
    public Result<String> updateAvatarUrl(@RequestBody UpdateAvatarRequest request) {
        String username = SecurityUtil.getCurrentUsername();
        User user = userService.lambdaQuery()
            .eq(User::getUsername, username)
            .one();

        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        String avatarUrl = request.getAvatarUrl();
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            return Result.error(400, "头像URL不能为空");
        }

        if (!avatarUrl.startsWith("http://") && !avatarUrl.startsWith("https://")) {
            return Result.error(400, "头像URL格式不正确，必须以http://或https://开头");
        }

        user.setAvatar(avatarUrl.trim());
        userService.updateById(user);

        return Result.success(avatarUrl);
    }

    @Operation(summary = "获取登录记录")
    @GetMapping("/login-records")
    public Result<Page<LoginRecordResponse>> getLoginRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        String username = SecurityUtil.getCurrentUsername();
        User user = userService.lambdaQuery()
            .eq(User::getUsername, username)
            .one();

        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Page<LoginRecord> recordPage = loginRecordService.page(
            new Page<>(page, size),
            new LambdaQueryWrapper<LoginRecord>()
                .eq(LoginRecord::getUserId, user.getId())
                .orderByDesc(LoginRecord::getLoginTime)
        );

        Page<LoginRecordResponse> responsePage = new Page<>(recordPage.getCurrent(), recordPage.getSize(), recordPage.getTotal());
        responsePage.setRecords(
            recordPage.getRecords().stream()
                .map(record -> LoginRecordResponse.builder()
                    .id(record.getId())
                    .loginTime(record.getLoginTime())
                    .ipAddress(record.getIpAddress())
                    .userAgent(record.getUserAgent())
                    .success(record.getSuccess())
                    .build())
                .toList()
        );

        return Result.success(responsePage);
    }

}

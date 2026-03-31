package com.tutoring.controller;

import com.tutoring.annotation.Log;
import com.tutoring.common.Result;
import com.tutoring.dto.LoginRequest;
import com.tutoring.dto.RegisterRequest;
import com.tutoring.entity.User;
import com.tutoring.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证管理", description = "登录注册接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "用户登录")
    @Log(module = "认证管理", operation = "登录", value = "用户登录")
    @PostMapping("/login")
    public Result<Void> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(null);
    }

    @Operation(summary = "用户注册")
    @Log(module = "认证管理", operation = "注册", value = "用户注册")
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterRequest request) {
        User existUser = userService.lambdaQuery()
            .eq(User::getUsername, request.getUsername())
            .one();
        if (existUser != null) {
            return Result.error(400, "用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole() != null ? request.getRole() : "STUDENT");
        user.setStatus(0);
        user.setAvatar(null);
        user.setDeleted(0);

        userService.save(user);

        return Result.success("注册成功，请等待管理员审核");
    }
}

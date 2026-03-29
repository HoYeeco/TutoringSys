package com.tutoring.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.common.Result;
import com.tutoring.dto.CreateUserRequest;
import com.tutoring.dto.UpdateUserRequest;
import com.tutoring.dto.UserVO;
import com.tutoring.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员-用户管理", description = "管理员对用户的增删改查及启用/禁用")
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "获取用户列表", description = "分页查询用户，支持按角色、状态、关键字筛选")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<UserVO>> listUsers(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "角色筛选") @RequestParam(required = false) String role,
            @Parameter(description = "状态筛选") @RequestParam(required = false) Integer status,
            @Parameter(description = "关键字搜索") @RequestParam(required = false) String keyword) {
        Page<UserVO> userPage = adminUserService.listUsers(page, size, role, status, keyword);
        return Result.success(userPage);
    }

    @Operation(summary = "获取用户详情", description = "根据ID获取用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        UserVO user = adminUserService.getUserById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(user);
    }

    @Operation(summary = "创建用户", description = "管理员创建新用户")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserVO> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            UserVO user = adminUserService.createUser(request);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "更新用户", description = "修改用户信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserVO> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        try {
            UserVO user = adminUserService.updateUser(id, request);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "删除用户", description = "软删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        try {
            adminUserService.deleteUser(id);
            return Result.success(null);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "更新用户状态", description = "启用/禁用用户")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            adminUserService.updateUserStatus(id, status);
            return Result.success(null);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}

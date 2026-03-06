package com.tutoringsys.api.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutoringsys.api.dto.AdminUserCreateDto;
import com.tutoringsys.api.dto.AdminUserUpdateDto;
import com.tutoringsys.api.dto.AdminUserVo;
import com.tutoringsys.common.response.R;
import com.tutoringsys.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "管理员用户管理", description = "管理员用户相关接口")
public class AdminUserController {

    @Resource
    private UserService userService;

    @GetMapping
    @Operation(summary = "获取用户列表", description = "获取用户列表，支持分页和筛选")
    public R<IPage<AdminUserVo>> pageUsers(
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小，默认10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "用户角色") @RequestParam(required = false) String role,
            @Parameter(description = "用户状态") @RequestParam(required = false) Integer status) {
        Map<String, Object> params = new HashMap<>();
        if (username != null) params.put("username", username);
        if (role != null) params.put("role", role);
        if (status != null) params.put("status", status);
        IPage<AdminUserVo> userPage = userService.pageUsers(page, size, params);
        return R.ok(userPage);
    }

    @PostMapping
    @Operation(summary = "创建用户", description = "创建新用户")
    public R<AdminUserVo> createUser(
            @Parameter(description = "用户创建数据") @RequestBody AdminUserCreateDto dto) {
        var user = userService.createUser(dto);
        AdminUserVo vo = new AdminUserVo();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        return R.ok(vo);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "更新用户信息")
    public R<AdminUserVo> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id, 
            @Parameter(description = "用户更新数据") @RequestBody AdminUserUpdateDto dto) {
        var user = userService.updateUser(id, dto);
        AdminUserVo vo = new AdminUserVo();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        return R.ok(vo);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "删除指定用户")
    public R<Void> deleteUser(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return R.ok();
    }

    @PostMapping("/{id}/reset-password")
    @Operation(summary = "重置密码", description = "重置指定用户的密码")
    public R<Map<String, String>> resetPassword(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        Map<String, String> result = userService.resetPassword(id);
        return R.ok(result);
    }
}
package com.tutoring.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.common.Result;
import com.tutoring.dto.CreateConfigRequest;
import com.tutoring.dto.SystemConfigVO;
import com.tutoring.dto.UpdateConfigRequest;
import com.tutoring.service.AdminConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员-系统配置", description = "系统配置管理及缓存清除")
@RestController
@RequestMapping("/admin/config")
@RequiredArgsConstructor
public class AdminConfigController {

    private final AdminConfigService adminConfigService;

    @Operation(summary = "获取配置列表", description = "分页查询系统配置，支持按类型和关键字筛选")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<SystemConfigVO>> listConfigs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "配置类型") @RequestParam(required = false) String configType,
            @Parameter(description = "关键字搜索") @RequestParam(required = false) String keyword) {
        Page<SystemConfigVO> configPage = adminConfigService.listConfigs(page, size, configType, keyword);
        return Result.success(configPage);
    }

    @Operation(summary = "获取配置详情", description = "根据ID获取配置详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SystemConfigVO> getConfigById(@PathVariable Long id) {
        SystemConfigVO config = adminConfigService.getConfigById(id);
        if (config == null) {
            return Result.error(404, "配置不存在");
        }
        return Result.success(config);
    }

    @Operation(summary = "根据Key获取配置", description = "根据配置键获取配置值")
    @GetMapping("/key/{configKey}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SystemConfigVO> getConfigByKey(@PathVariable String configKey) {
        SystemConfigVO config = adminConfigService.getConfigByKey(configKey);
        if (config == null) {
            return Result.error(404, "配置不存在");
        }
        return Result.success(config);
    }

    @Operation(summary = "创建配置", description = "新增系统配置")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SystemConfigVO> createConfig(@Valid @RequestBody CreateConfigRequest request) {
        try {
            SystemConfigVO config = adminConfigService.createConfig(request);
            return Result.success(config);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "更新配置", description = "修改系统配置")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SystemConfigVO> updateConfig(@PathVariable Long id, @Valid @RequestBody UpdateConfigRequest request) {
        try {
            SystemConfigVO config = adminConfigService.updateConfig(id, request);
            return Result.success(config);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "删除配置", description = "删除系统配置")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteConfig(@PathVariable Long id) {
        try {
            adminConfigService.deleteConfig(id);
            return Result.success(null);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "清除缓存", description = "清除Redis中的配置缓存，可指定前缀")
    @PostMapping("/clear-cache")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> clearCache(
            @Parameter(description = "缓存键前缀（可选）") @RequestParam(required = false) String prefix) {
        adminConfigService.clearCache(prefix);
        String message = StringUtils.hasText(prefix) 
            ? "已清除前缀为 '" + prefix + "' 的配置缓存" 
            : "已清除所有配置缓存";
        return Result.success(message);
    }
}

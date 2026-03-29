package com.tutoring.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.common.Result;
import com.tutoring.dto.AuditLogVO;
import com.tutoring.dto.LLMMonitorVO;
import com.tutoring.dto.LoginRecordVO;
import com.tutoring.dto.RedisMonitorVO;
import com.tutoring.service.AdminMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "管理员-系统监控", description = "大模型监控、Redis监控、日志审计")
@RestController
@RequestMapping("/admin/monitor")
@RequiredArgsConstructor
public class AdminMonitorController {

    private final AdminMonitorService adminMonitorService;

    @Operation(summary = "大模型监控", description = "获取LLM调用统计、Token消耗、失败记录")
    @GetMapping("/llm")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<LLMMonitorVO> getLLMMonitor() {
        LLMMonitorVO monitor = adminMonitorService.getLLMMonitor();
        return Result.success(monitor);
    }

    @Operation(summary = "Redis监控", description = "获取Redis运行状态、内存使用、命中率")
    @GetMapping("/redis")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<RedisMonitorVO> getRedisMonitor() {
        RedisMonitorVO monitor = adminMonitorService.getRedisMonitor();
        return Result.success(monitor);
    }

    @Operation(summary = "操作日志列表", description = "分页查询操作日志，支持按操作人、操作类型、时间范围筛选")
    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<AuditLogVO>> listAuditLogs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "操作人") @RequestParam(required = false) String operator,
            @Parameter(description = "操作类型") @RequestParam(required = false) String operationType,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        Page<AuditLogVO> logPage = adminMonitorService.listAuditLogs(
            page, size, operator, operationType, startTime, endTime);
        return Result.success(logPage);
    }

    @Operation(summary = "登录日志列表", description = "分页查询登录记录，支持按用户、时间范围筛选")
    @GetMapping("/login-records")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<LoginRecordVO>> listLoginRecords(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        Page<LoginRecordVO> recordPage = adminMonitorService.listLoginRecords(
            page, size, userId, startTime, endTime);
        return Result.success(recordPage);
    }
}

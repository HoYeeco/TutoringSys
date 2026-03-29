package com.tutoring.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.common.Result;
import com.tutoring.dto.SubmissionDetailVO;
import com.tutoring.dto.SubmissionRecordVO;
import com.tutoring.service.AdminSubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "管理员-作业监控", description = "查看所有作业提交记录及批改详情")
@RestController
@RequestMapping("/admin/submissions")
@RequiredArgsConstructor
public class AdminSubmissionController {

    private final AdminSubmissionService adminSubmissionService;

    @Operation(summary = "获取提交记录列表", description = "分页查询所有提交记录，支持按课程、批改状态、时间范围筛选")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<SubmissionRecordVO>> listSubmissions(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "课程ID筛选") @RequestParam(required = false) Long courseId,
            @Parameter(description = "批改状态：0-待批改，1-已批改") @RequestParam(required = false) Integer reviewStatus,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        Page<SubmissionRecordVO> submissionPage = adminSubmissionService.listSubmissions(
            page, size, courseId, reviewStatus, startTime, endTime);
        return Result.success(submissionPage);
    }

    @Operation(summary = "获取提交详情", description = "查看提交的详细批改日志，包括AI反馈和教师反馈")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SubmissionDetailVO> getSubmissionDetail(@PathVariable Long id) {
        SubmissionDetailVO detail = adminSubmissionService.getSubmissionDetail(id);
        if (detail == null) {
            return Result.error(404, "提交记录不存在");
        }
        return Result.success(detail);
    }
}

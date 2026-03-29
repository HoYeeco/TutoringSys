package com.tutoring.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.common.Result;
import com.tutoring.dto.AdminAssignmentVO;
import com.tutoring.service.AdminAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员-作业管理", description = "管理员查看和管理所有作业")
@RestController
@RequestMapping("/admin/assignments")
@RequiredArgsConstructor
public class AdminAssignmentController {

    private final AdminAssignmentService adminAssignmentService;

    @Operation(summary = "获取作业列表", description = "分页查询所有作业，支持按课程、状态、关键字筛选")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<AdminAssignmentVO>> listAssignments(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "课程ID筛选") @RequestParam(required = false) Long courseId,
            @Parameter(description = "状态筛选") @RequestParam(required = false) String status,
            @Parameter(description = "关键字搜索") @RequestParam(required = false) String keyword) {
        Page<AdminAssignmentVO> assignmentPage = adminAssignmentService.listAssignments(
            page, size, courseId, status, keyword);
        return Result.success(assignmentPage);
    }

    @Operation(summary = "获取作业详情", description = "根据ID获取作业详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<AdminAssignmentVO> getAssignmentDetail(@PathVariable Long id) {
        AdminAssignmentVO assignment = adminAssignmentService.getAssignmentDetail(id);
        if (assignment == null) {
            return Result.error(404, "作业不存在");
        }
        return Result.success(assignment);
    }

    @Operation(summary = "删除作业", description = "删除作业及其关联数据")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteAssignment(@PathVariable Long id) {
        try {
            adminAssignmentService.deleteAssignment(id);
            return Result.success(null);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}

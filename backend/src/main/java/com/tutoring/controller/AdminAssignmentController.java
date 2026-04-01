package com.tutoring.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.common.Result;
import com.tutoring.dto.AdminAssignmentVO;
import com.tutoring.dto.CreateAssignmentRequest;
import com.tutoring.dto.SubmissionDetailVO;
import com.tutoring.dto.SubmissionRecordVO;
import com.tutoring.dto.UpdateAssignmentRequest;
import com.tutoring.service.AdminAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    @Operation(summary = "创建作业", description = "管理员创建作业")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> createAssignment(@Valid @RequestBody CreateAssignmentRequest request) {
        Long assignmentId = adminAssignmentService.createAssignment(request);
        return Result.success(assignmentId);
    }

    @Operation(summary = "更新作业", description = "管理员更新作业")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateAssignment(@Valid @RequestBody UpdateAssignmentRequest request) {
        adminAssignmentService.updateAssignment(request.getId(), request);
        return Result.success(null);
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

    @Operation(summary = "删除学生提交", description = "管理员删除学生的作业提交记录")
    @DeleteMapping("/submissions/{submissionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteSubmission(@PathVariable Long submissionId) {
        try {
            adminAssignmentService.deleteSubmission(submissionId);
            return Result.success(null);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "打回重做", description = "管理员将学生作业打回，让学生重新提交")
    @PostMapping("/submissions/{submissionId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> rejectSubmission(@PathVariable Long submissionId) {
        try {
            adminAssignmentService.rejectSubmission(submissionId);
            return Result.success(null);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "获取作业提交列表", description = "管理员获取指定作业的学生提交列表")
    @GetMapping("/{id}/submissions")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<SubmissionRecordVO>> getAssignmentSubmissions(
            @Parameter(description = "作业ID") @PathVariable Long id,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        try {
            Page<SubmissionRecordVO> submissionPage = adminAssignmentService.getAssignmentSubmissions(id, page, size);
            return Result.success(submissionPage);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "获取提交详情", description = "管理员获取学生提交的详细信息")
    @GetMapping("/submissions/{submissionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SubmissionDetailVO> getSubmissionDetail(@PathVariable Long submissionId) {
        SubmissionDetailVO detail = adminAssignmentService.getSubmissionDetail(submissionId);
        if (detail == null) {
            return Result.error(404, "提交记录不存在");
        }
        return Result.success(detail);
    }
}

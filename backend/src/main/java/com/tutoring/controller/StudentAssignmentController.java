package com.tutoring.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.common.Result;
import com.tutoring.dto.AssignmentDetailVO;
import com.tutoring.dto.SaveDraftRequest;
import com.tutoring.dto.StudentAssignmentListVO;
import com.tutoring.dto.SubmitRequest;
import com.tutoring.dto.SubmitResponse;
import com.tutoring.service.StudentAssignmentService;
import com.tutoring.service.UserService;
import com.tutoring.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "学生作业", description = "学生作业相关接口")
@RestController
@RequestMapping("/student/assignments")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class StudentAssignmentController {

    private final StudentAssignmentService studentAssignmentService;
    private final UserService userService;

    @Operation(summary = "获取作业列表")
    @GetMapping("/list")
    public Result<Page<StudentAssignmentListVO>> getAssignmentList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "deadline") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {
        
        Long studentId = SecurityUtil.getCurrentUserId(userService);
        Page<StudentAssignmentListVO> result = studentAssignmentService.getAssignmentList(
            studentId, page, size, status, courseId, keyword, sortBy, sortOrder);
        return Result.success(result);
    }

    @Operation(summary = "获取作业详情")
    @GetMapping("/{assignmentId}/detail")
    public Result<AssignmentDetailVO> getAssignmentDetail(@PathVariable Long assignmentId) {
        Long studentId = SecurityUtil.getCurrentUserId(userService);
        AssignmentDetailVO detail = studentAssignmentService.getAssignmentDetail(studentId, assignmentId);
        return Result.success(detail);
    }

    @Operation(summary = "保存草稿")
    @PostMapping("/{assignmentId}/save-draft")
    public Result<Void> saveDraft(
            @PathVariable Long assignmentId,
            @Valid @RequestBody SaveDraftRequest request) {
        Long studentId = SecurityUtil.getCurrentUserId(userService);
        studentAssignmentService.saveDraft(studentId, assignmentId, request);
        return Result.success(null);
    }

    @Operation(summary = "获取草稿")
    @GetMapping("/{assignmentId}/draft")
    public Result<Object> getDraft(@PathVariable Long assignmentId) {
        Long studentId = SecurityUtil.getCurrentUserId(userService);
        Object draft = studentAssignmentService.getDraft(studentId, assignmentId);
        return Result.success(draft);
    }

    @Operation(summary = "提交作业")
    @PostMapping("/{assignmentId}/submit")
    public Result<SubmitResponse> submitAssignment(
            @PathVariable Long assignmentId,
            @Valid @RequestBody SubmitRequest request) {
        Long studentId = SecurityUtil.getCurrentUserId(userService);
        SubmitResponse response = studentAssignmentService.submitAssignment(studentId, assignmentId, request);
        return Result.success(response);
    }

}

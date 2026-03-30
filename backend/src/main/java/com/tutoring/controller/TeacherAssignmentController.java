package com.tutoring.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.common.Result;
import com.tutoring.dto.AssignmentSubmissionStatusVO;
import com.tutoring.dto.CreateAssignmentRequest;
import com.tutoring.dto.ReviewSubmissionRequest;
import com.tutoring.dto.SubmissionDetailVO;
import com.tutoring.dto.SubmissionRecordVO;
import com.tutoring.dto.TeacherAssignmentVO;
import com.tutoring.dto.UpdateAssignmentRequest;
import com.tutoring.entity.User;
import com.tutoring.service.TeacherAssignmentService;
import com.tutoring.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "教师作业管理", description = "教师作业管理相关接口")
@RestController
@RequestMapping("/teacher/assignments")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class TeacherAssignmentController {

    private final TeacherAssignmentService teacherAssignmentService;
    private final UserService userService;

    @Operation(summary = "获取作业列表")
    @GetMapping("/list")
    public Result<Page<TeacherAssignmentVO>> getAssignmentList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String keyword) {
        
        Long teacherId = getCurrentUserId();
        Page<TeacherAssignmentVO> result = teacherAssignmentService.getAssignmentList(
            teacherId, page, size, status, courseId, keyword);
        return Result.success(result);
    }

    @Operation(summary = "获取作业详情")
    @GetMapping("/{assignmentId}")
    public Result<TeacherAssignmentVO> getAssignmentDetail(@PathVariable Long assignmentId) {
        Long teacherId = getCurrentUserId();
        TeacherAssignmentVO detail = teacherAssignmentService.getAssignmentDetail(teacherId, assignmentId);
        return Result.success(detail);
    }

    @Operation(summary = "创建作业")
    @PostMapping
    public Result<Long> createAssignment(@Valid @RequestBody CreateAssignmentRequest request) {
        Long teacherId = getCurrentUserId();
        Long assignmentId = teacherAssignmentService.createAssignment(teacherId, request);
        return Result.success(assignmentId);
    }

    @Operation(summary = "更新作业")
    @PutMapping
    public Result<Void> updateAssignment(@Valid @RequestBody UpdateAssignmentRequest request) {
        Long teacherId = getCurrentUserId();
        teacherAssignmentService.updateAssignment(teacherId, request);
        return Result.success(null);
    }

    @Operation(summary = "发布作业")
    @PostMapping("/{assignmentId}/publish")
    public Result<Void> publishAssignment(@PathVariable Long assignmentId) {
        Long teacherId = getCurrentUserId();
        teacherAssignmentService.publishAssignment(teacherId, assignmentId);
        return Result.success(null);
    }

    @Operation(summary = "删除作业")
    @DeleteMapping("/{assignmentId}")
    public Result<Void> deleteAssignment(@PathVariable Long assignmentId) {
        Long teacherId = getCurrentUserId();
        teacherAssignmentService.deleteAssignment(teacherId, assignmentId);
        return Result.success(null);
    }

    @Operation(summary = "获取作业提交列表")
    @GetMapping("/{assignmentId}/submissions")
    public Result<Page<SubmissionRecordVO>> getAssignmentSubmissions(
            @PathVariable Long assignmentId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long teacherId = getCurrentUserId();
        Page<SubmissionRecordVO> result = teacherAssignmentService.getAssignmentSubmissions(
                teacherId, assignmentId, page, size);
        return Result.success(result);
    }

    @Operation(summary = "获取作业提交状态")
    @GetMapping("/{assignmentId}/submission-status")
    public Result<AssignmentSubmissionStatusVO> getAssignmentSubmissionStatus(@PathVariable Long assignmentId) {
        Long teacherId = getCurrentUserId();
        AssignmentSubmissionStatusVO result = teacherAssignmentService.getAssignmentSubmissionStatus(teacherId, assignmentId);
        return Result.success(result);
    }

    @Operation(summary = "获取提交详情")
    @GetMapping("/submissions/{submissionId}")
    public Result<SubmissionDetailVO> getSubmissionDetail(@PathVariable Long submissionId) {
        Long teacherId = getCurrentUserId();
        SubmissionDetailVO detail = teacherAssignmentService.getSubmissionDetail(teacherId, submissionId);
        if (detail == null) {
            return Result.error(404, "提交记录不存在");
        }
        return Result.success(detail);
    }

    @Operation(summary = "批改提交")
    @PutMapping("/submissions/{submissionId}/review")
    public Result<Void> reviewSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody ReviewSubmissionRequest request) {
        Long teacherId = getCurrentUserId();
        teacherAssignmentService.reviewSubmission(teacherId, submissionId, request);
        return Result.success(null);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未认证");
        }
        String username = authentication.getName();
        User user = userService.lambdaQuery()
            .eq(User::getUsername, username)
            .one();
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user.getId();
    }

}

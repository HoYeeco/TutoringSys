package com.tutoring.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.common.Result;
import com.tutoring.dto.GradingDetailVO;
import com.tutoring.dto.GradingListVO;
import com.tutoring.entity.Submission;
import com.tutoring.event.SubmissionEvent;
import com.tutoring.exception.BusinessException;
import com.tutoring.mapper.SubmissionMapper;
import com.tutoring.service.StudentGradingService;
import com.tutoring.service.UserService;
import com.tutoring.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

@Tag(name = "学生成绩", description = "学生成绩批改相关接口")
@RestController
@RequestMapping("/student/grading")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class StudentGradingController {

    private final StudentGradingService studentGradingService;
    private final UserService userService;
    private final SubmissionMapper submissionMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Operation(summary = "获取批改列表")
    @GetMapping("/list")
    public Result<Page<GradingListVO>> getGradingList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "reviewTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        
        Long studentId = SecurityUtil.getCurrentUserId(userService);
        Page<GradingListVO> result = studentGradingService.getGradingList(
            studentId, page, size, courseId, keyword, sortBy, sortOrder);
        return Result.success(result);
    }

    @Operation(summary = "获取批改详情")
    @GetMapping("/{submissionId}")
    public Result<GradingDetailVO> getGradingDetail(@PathVariable Long submissionId) {
        Long studentId = SecurityUtil.getCurrentUserId(userService);
        GradingDetailVO detail = studentGradingService.getGradingDetail(studentId, submissionId);
        return Result.success(detail);
    }

    @Operation(summary = "重新触发自动批改")
    @PostMapping("/{submissionId}/regrade")
    public Result<Void> retriggerGrading(@PathVariable Long submissionId) {
        Long studentId = SecurityUtil.getCurrentUserId(userService);
        
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException("提交记录不存在");
        }
        if (!submission.getStudentId().equals(studentId)) {
            throw new BusinessException("无权操作此提交记录");
        }
        
        eventPublisher.publishEvent(new SubmissionEvent(
            submission.getId(), 
            studentId, 
            submission.getAssignmentId()
        ));
        
        return Result.success(null);
    }

}

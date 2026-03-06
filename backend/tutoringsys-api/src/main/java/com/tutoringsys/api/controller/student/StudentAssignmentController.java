package com.tutoringsys.api.controller.student;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutoringsys.api.dto.GradingReportVo;
import com.tutoringsys.api.dto.StudentAssignmentVo;
import com.tutoringsys.api.dto.SubmissionDto;
import com.tutoringsys.api.dto.SubmissionListItemVo;
import com.tutoringsys.common.response.R;
import com.tutoringsys.service.StudentAssignmentService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/student/assignments")
@PreAuthorize("hasRole('STUDENT')")
@Tag(name = "学生作业管理", description = "学生作业相关接口")
public class StudentAssignmentController {

    @Resource
    private StudentAssignmentService studentAssignmentService;

    @GetMapping("/list")
    @Operation(summary = "获取作业列表", description = "获取学生的作业列表，支持分页和状态筛选")
    public R<IPage<StudentAssignmentVo>> getAssignmentList(
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小，默认10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "作业状态，可选值：pending, graded") @RequestParam(required = false) String status) {
        IPage<StudentAssignmentVo> assignmentPage = studentAssignmentService.getAssignmentList(page, size, status);
        return R.ok(assignmentPage);
    }

    @GetMapping("/history")
    @Operation(summary = "获取历史记录", description = "获取学生的作业提交历史记录")
    public R<IPage<SubmissionListItemVo>> getHistoryRecord(
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小，默认10") @RequestParam(defaultValue = "10") int size) {
        IPage<SubmissionListItemVo> historyPage = studentAssignmentService.getSubmissionHistory(page, size);
        return R.ok(historyPage);
    }

    @PostMapping("/submit/{assignmentId}")
    @RateLimiter(name = "submitAssignment")
    @Operation(summary = "提交作业", description = "提交作业答案，支持客观题和主观题")
    public R<GradingReportVo> submitAssignment(
            @Parameter(description = "作业ID") @PathVariable Long assignmentId, 
            @Parameter(description = "作业提交数据") @RequestBody SubmissionDto dto) {
        dto.setAssignmentId(assignmentId);
        GradingReportVo result = studentAssignmentService.submitAssignment(dto);
        return R.ok(result);
    }

    @GetMapping("/report/{submissionId}")
    @Operation(summary = "获取批改报告", description = "获取作业批改报告，包含得分和反馈")
    public R<GradingReportVo> getGradingReport(
            @Parameter(description = "提交记录ID") @PathVariable String submissionId) {
        GradingReportVo report = studentAssignmentService.getSubmissionReport(submissionId);
        return R.ok(report);
    }
}
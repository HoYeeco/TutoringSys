package com.tutoringsys.api.controller.student;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutoringsys.common.dto.AssignmentListVo;
import com.tutoringsys.common.dto.GradingReportVo;
import com.tutoringsys.common.dto.SubmissionDto;
import com.tutoringsys.common.response.R;
import com.tutoringsys.common.util.SecurityUtil;
import com.tutoringsys.service.StudentAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/student/assignments")
@Tag(name = "学生作业管理", description = "学生作业相关接口")
public class StudentAssignmentController {

    @Resource
    private StudentAssignmentService studentAssignmentService;

    @PostMapping("/submit/{assignmentId}")
    @Operation(summary = "提交作业", description = "提交作业答案，系统自动批改")
    public R<GradingReportVo> submitAssignment(
            @Parameter(description = "作业ID") @PathVariable Long assignmentId,
            @Parameter(description = "提交数据") @RequestBody SubmissionDto dto) {
        Long studentId = SecurityUtil.getCurrentUserId();
        if (dto.getSubmissionId() == null || dto.getSubmissionId().isEmpty()) {
            dto.setSubmissionId(UUID.randomUUID().toString());
        }
        GradingReportVo report = studentAssignmentService.submitAssignment(assignmentId, studentId, dto);
        return R.ok(report);
    }

    @PostMapping("/draft/{assignmentId}")
    @Operation(summary = "保存草稿", description = "保存作业草稿")
    public R<Boolean> saveDraft(
            @Parameter(description = "作业ID") @PathVariable Long assignmentId,
            @Parameter(description = "草稿数据") @RequestBody SubmissionDto dto) {
        Long studentId = SecurityUtil.getCurrentUserId();
        boolean result = studentAssignmentService.saveDraft(assignmentId, studentId, dto);
        return R.ok(result);
    }

    @GetMapping("/draft/{assignmentId}")
    @Operation(summary = "获取草稿", description = "获取学生作业草稿")
    public R<SubmissionDto> getDraft(
            @Parameter(description = "作业ID") @PathVariable Long assignmentId) {
        Long studentId = SecurityUtil.getCurrentUserId();
        SubmissionDto draft = studentAssignmentService.getDraft(assignmentId, studentId);
        return R.ok(draft);
    }

    @GetMapping("/list")
    @Operation(summary = "获取作业列表", description = "获取学生作业列表，支持分页和筛选")
    public R<IPage<AssignmentListVo>> getAssignmentList(
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小，默认10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "作业状态") @RequestParam(required = false) String status) {
        Long studentId = SecurityUtil.getCurrentUserId();
        IPage<AssignmentListVo> assignmentPage = studentAssignmentService.getAssignmentList(studentId, page, size, status);
        return R.ok(assignmentPage);
    }

    @GetMapping("/{assignmentId}")
    @Operation(summary = "获取作业详情", description = "获取作业详情及题目列表")
    public R<Map<String, Object>> getAssignmentDetail(
            @Parameter(description = "作业ID") @PathVariable Long assignmentId) {
        Long studentId = SecurityUtil.getCurrentUserId();
        Map<String, Object> detail = studentAssignmentService.getAssignmentDetail(assignmentId, studentId);
        return R.ok(detail);
    }
}

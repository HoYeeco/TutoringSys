package com.tutoringsys.api.controller.teacher;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutoringsys.api.dto.AssignmentCreateDto;
import com.tutoringsys.api.dto.AssignmentUpdateDto;
import com.tutoringsys.api.dto.AssignmentVo;
import com.tutoringsys.api.dto.GradingReportVo;
import com.tutoringsys.api.dto.ReviewDto;
import com.tutoringsys.api.dto.SubmissionListItemVo;
import com.tutoringsys.common.response.R;
import com.tutoringsys.service.TeacherAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/teacher/assignments")
@PreAuthorize("hasRole('TEACHER')")
@Tag(name = "教师作业管理", description = "教师作业相关接口")
public class TeacherAssignmentController {

    @Resource
    private TeacherAssignmentService teacherAssignmentService;

    @PostMapping
    @Operation(summary = "创建作业", description = "创建新的作业，包含题目和评分标准")
    public R<AssignmentVo> createAssignment(
            @Parameter(description = "作业创建数据") @RequestBody AssignmentCreateDto dto) {
        AssignmentVo assignment = teacherAssignmentService.createAssignment(dto);
        return R.ok(assignment);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新作业", description = "更新作业信息，包括题目和评分标准")
    public R<AssignmentVo> updateAssignment(
            @Parameter(description = "作业ID") @PathVariable Long id, 
            @Parameter(description = "作业更新数据") @RequestBody AssignmentUpdateDto dto) {
        AssignmentVo assignment = teacherAssignmentService.updateAssignment(id, dto);
        return R.ok(assignment);
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "发布作业", description = "将作业发布给学生")
    public R<AssignmentVo> publishAssignment(
            @Parameter(description = "作业ID") @PathVariable Long id) {
        AssignmentVo assignment = teacherAssignmentService.publishAssignment(id);
        return R.ok(assignment);
    }

    @GetMapping("/{assignmentId}/submissions")
    @Operation(summary = "获取作业提交列表", description = "获取指定作业的学生提交列表，支持分页")
    public R<IPage<SubmissionListItemVo>> getSubmissionList(
            @Parameter(description = "作业ID") @PathVariable Long assignmentId,
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小，默认10") @RequestParam(defaultValue = "10") int size) {
        IPage<SubmissionListItemVo> submissionPage = teacherAssignmentService.getSubmissionList(assignmentId, page, size);
        return R.ok(submissionPage);
    }

    @GetMapping("/submissions/{submissionId}/detail")
    @Operation(summary = "获取提交详情", description = "获取学生作业提交的详细信息，包括答案和批改结果")
    public R<GradingReportVo> getSubmissionDetail(
            @Parameter(description = "提交记录ID") @PathVariable String submissionId) {
        GradingReportVo report = teacherAssignmentService.getSubmissionDetail(submissionId);
        return R.ok(report);
    }

    @PostMapping("/submissions/{submissionId}/review")
    @Operation(summary = "复核作业", description = "教师复核学生作业，修改评分和反馈")
    public R<Boolean> reviewSubmission(
            @Parameter(description = "提交记录ID") @PathVariable String submissionId, 
            @Parameter(description = "复核数据") @RequestBody ReviewDto dto) {
        dto.setSubmissionId(submissionId);
        boolean result = teacherAssignmentService.reviewSubmission(dto);
        return R.ok(result);
    }
}
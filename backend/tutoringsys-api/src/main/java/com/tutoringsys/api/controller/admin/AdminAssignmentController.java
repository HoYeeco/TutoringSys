package com.tutoringsys.api.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutoringsys.common.response.R;
import com.tutoringsys.dao.entity.Assignment;
import com.tutoringsys.service.AdminAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/admin/assignments")
@Tag(name = "管理员作业管理", description = "管理员作业相关接口")
public class AdminAssignmentController {

    @Resource
    private AdminAssignmentService adminAssignmentService;

    @GetMapping
    @Operation(summary = "获取作业列表", description = "获取作业列表，支持分页和筛选")
    public R<IPage<Map<String, Object>>> getAssignmentList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        IPage<Map<String, Object>> result = adminAssignmentService.getAssignmentList(page, pageSize, courseId, teacherId, status, keyword);
        return R.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取作业详情", description = "获取作业详细信息")
    public R<Map<String, Object>> getAssignmentDetail(@PathVariable Long id) {
        Map<String, Object> detail = adminAssignmentService.getAssignmentDetail(id);
        return R.ok(detail);
    }

    @PostMapping
    @Operation(summary = "创建作业", description = "创建新作业")
    public R<Assignment> createAssignment(@RequestBody Map<String, Object> data) {
        Assignment assignment = adminAssignmentService.createAssignment(data);
        return R.ok(assignment);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新作业", description = "更新作业信息")
    public R<Assignment> updateAssignment(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        Assignment assignment = adminAssignmentService.updateAssignment(id, data);
        return R.ok(assignment);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除作业", description = "删除作业")
    public R<Boolean> deleteAssignment(@PathVariable Long id) {
        boolean result = adminAssignmentService.deleteAssignment(id);
        return R.ok(result);
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "发布作业", description = "发布作业给学生")
    public R<Boolean> publishAssignment(@PathVariable Long id) {
        boolean result = adminAssignmentService.publishAssignment(id);
        return R.ok(result);
    }

    @GetMapping("/{id}/submission-status")
    @Operation(summary = "获取作业提交情况", description = "获取作业的学生提交情况，包括已交和未交学生列表")
    public R<Map<String, Object>> getSubmissionStatus(@PathVariable Long id) {
        Map<String, Object> result = adminAssignmentService.getAssignmentSubmissionStatus(id);
        return R.ok(result);
    }

    @PostMapping("/{id}/remind")
    @Operation(summary = "提醒学生", description = "提醒未提交作业的学生")
    public R<Boolean> remindStudents(@PathVariable Long id, @RequestParam(required = false) Long studentId) {
        boolean result = adminAssignmentService.remindStudents(id, studentId);
        return R.ok(result);
    }
}

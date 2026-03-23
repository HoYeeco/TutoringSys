package com.tutoringsys.api.controller.student;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutoringsys.common.dto.GradingDetailVo;
import com.tutoringsys.common.dto.GradingListVo;
import com.tutoringsys.common.response.R;
import com.tutoringsys.common.util.SecurityUtil;
import com.tutoringsys.service.StudentGradingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/student/grading")
@Tag(name = "学生成绩批改", description = "学生成绩批改相关接口")
public class StudentGradingController {

    @Resource
    private StudentGradingService studentGradingService;

    @GetMapping("/list")
    @Operation(summary = "获取批改列表", description = "获取学生已批改作业列表，支持分页和筛选")
    public R<Map<String, Object>> getGradingList(
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小，默认10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "课程ID") @RequestParam(required = false) Long courseId,
            @Parameter(description = "排序字段") @RequestParam(required = false) String sortBy,
            @Parameter(description = "排序方向") @RequestParam(required = false) String sortOrder) {
        Long studentId = SecurityUtil.getCurrentUserId();
        Map<String, Object> result = studentGradingService.getGradingList(studentId, page, size, courseId, sortBy, sortOrder);
        return R.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取批改详情", description = "获取作业批改详情")
    public R<GradingDetailVo> getGradingDetail(
            @Parameter(description = "提交记录ID") @PathVariable Long id) {
        Long studentId = SecurityUtil.getCurrentUserId();
        GradingDetailVo detail = studentGradingService.getGradingDetail(id, studentId);
        return R.ok(detail);
    }

    @PostMapping("/error-book")
    @Operation(summary = "加入错题本", description = "将错题加入错题本")
    public R<Boolean> addToErrorBook(
            @Parameter(description = "题目ID") @RequestParam Long questionId) {
        Long studentId = SecurityUtil.getCurrentUserId();
        boolean result = studentGradingService.addToErrorBook(studentId, questionId);
        return R.ok(result);
    }

    @PostMapping("/error-book/batch")
    @Operation(summary = "批量加入错题本", description = "批量将错题加入错题本")
    public R<Boolean> batchAddToErrorBook(
            @Parameter(description = "题目ID列表") @RequestBody java.util.List<Long> questionIds) {
        Long studentId = SecurityUtil.getCurrentUserId();
        boolean result = studentGradingService.batchAddToErrorBook(studentId, questionIds);
        return R.ok(result);
    }
}

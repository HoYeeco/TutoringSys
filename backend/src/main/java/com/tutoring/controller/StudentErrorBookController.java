package com.tutoring.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.common.Result;
import com.tutoring.dto.ErrorBookListVO;
import com.tutoring.dto.ErrorBookStatVO;
import com.tutoring.service.StudentErrorBookService;
import com.tutoring.service.UserService;
import com.tutoring.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Tag(name = "学生错题本", description = "学生错题本相关接口")
@RestController
@RequestMapping("/student/error-book")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class StudentErrorBookController {

    private final StudentErrorBookService studentErrorBookService;
    private final UserService userService;

    @Operation(summary = "加入错题本")
    @PostMapping("/add")
    public Result<Void> addToErrorBook(@RequestBody Map<String, Object> body) {
        Long studentId = SecurityUtil.getCurrentUserId(userService);
        Long questionId = toLong(body.get("questionId"));
        Long assignmentId = toLong(body.get("assignmentId"));
        if (questionId == null || assignmentId == null) {
            return Result.error(400, "缺少必要参数: questionId 或 assignmentId");
        }
        studentErrorBookService.addToErrorBook(studentId, questionId, assignmentId);
        return Result.success(null);
    }

    @Operation(summary = "检查题目是否已在错题本中")
    @GetMapping("/check")
    public Result<Set<Long>> checkInErrorBook(
            @RequestParam Long assignmentId,
            @RequestParam List<Long> questionIds) {
        Long studentId = SecurityUtil.getCurrentUserId(userService);
        Set<Long> existingIds = studentErrorBookService.checkInErrorBook(studentId, assignmentId, questionIds);
        return Result.success(existingIds);
    }

    @Operation(summary = "批量加入错题本")
    @PostMapping("/add-batch")
    public Result<Map<String, Object>> batchAddToErrorBook(@RequestBody Map<String, Object> body) {
        Long studentId = SecurityUtil.getCurrentUserId(userService);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> questions = (List<Map<String, Object>>) body.get("questions");
        List<Map<String, Long>> converted = questions.stream()
            .map(m -> Map.of(
                "questionId", toLong(m.get("questionId")),
                "assignmentId", toLong(m.get("assignmentId"))
            ))
            .toList();
        Map<String, Object> result = studentErrorBookService.batchAddToErrorBook(studentId, converted);
        return Result.success(result);
    }

    @Operation(summary = "获取错题统计（按课程）")
    @GetMapping("/stats")
    public Result<List<ErrorBookStatVO>> getErrorBookStats() {
        Long studentId = SecurityUtil.getCurrentUserId(userService);
        List<ErrorBookStatVO> stats = studentErrorBookService.getErrorBookStats(studentId);
        return Result.success(stats);
    }

    @Operation(summary = "获取错题列表")
    @GetMapping("/list")
    public Result<Page<ErrorBookListVO>> getErrorBookList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {
        
        Long studentId = SecurityUtil.getCurrentUserId(userService);
        Page<ErrorBookListVO> result = studentErrorBookService.getErrorBookList(
            studentId, page, size, courseId, type, keyword);
        return Result.success(result);
    }

    @Operation(summary = "移除错题")
    @DeleteMapping("/{errorBookId}")
    public Result<Void> removeFromErrorBook(@PathVariable Long errorBookId) {
        Long studentId = SecurityUtil.getCurrentUserId(userService);
        studentErrorBookService.removeFromErrorBook(studentId, errorBookId);
        return Result.success(null);
    }

    @Operation(summary = "导出错题本")
    @GetMapping("/export")
    public void exportErrorBook(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "excel") String format,
            HttpServletResponse response) {
        
        Long studentId = SecurityUtil.getCurrentUserId(userService);
        studentErrorBookService.exportErrorBook(studentId, courseId, type, format, response);
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

}

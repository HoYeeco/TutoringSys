package com.tutoring.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.common.Result;
import com.tutoring.dto.ErrorAnalysisVO;
import com.tutoring.dto.FrequentErrorVO;
import com.tutoring.dto.OverviewStatsVO;
import com.tutoring.dto.StudentTrendVO;
import com.tutoring.entity.User;
import com.tutoring.service.TeacherAnalyticsService;
import com.tutoring.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "教师学情分析", description = "教师学情分析相关接口")
@RestController
@RequestMapping("/teacher/analytics")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class TeacherAnalyticsController {

    private final TeacherAnalyticsService teacherAnalyticsService;
    private final UserService userService;

    @Operation(summary = "获取总览看板")
    @GetMapping("/overview")
    public Result<OverviewStatsVO> getOverviewStats() {
        Long teacherId = getCurrentUserId();
        OverviewStatsVO stats = teacherAnalyticsService.getOverviewStats(teacherId);
        return Result.success(stats);
    }

    @Operation(summary = "获取错题分析")
    @GetMapping("/error-analysis")
    public Result<ErrorAnalysisVO> getErrorAnalysis(
            @RequestParam(required = false) Long courseId) {
        Long teacherId = getCurrentUserId();
        ErrorAnalysisVO analysis = teacherAnalyticsService.getErrorAnalysis(teacherId, courseId);
        return Result.success(analysis);
    }

    @Operation(summary = "获取高频错题列表")
    @GetMapping("/frequent-errors")
    public Result<Page<FrequentErrorVO>> getFrequentErrors(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long courseId) {
        Long teacherId = getCurrentUserId();
        Page<FrequentErrorVO> result = teacherAnalyticsService.getFrequentErrors(teacherId, page, size, courseId);
        return Result.success(result);
    }

    @Operation(summary = "导出高频错题")
    @GetMapping("/frequent-errors/export")
    public void exportFrequentErrors(
            @RequestParam(required = false) Long courseId,
            HttpServletResponse response) {
        Long teacherId = getCurrentUserId();
        teacherAnalyticsService.exportFrequentErrors(teacherId, courseId, response);
    }

    @Operation(summary = "获取学生个体分析")
    @GetMapping("/student/{studentId}/trend")
    public Result<StudentTrendVO> getStudentTrend(@PathVariable Long studentId) {
        Long teacherId = getCurrentUserId();
        StudentTrendVO trend = teacherAnalyticsService.getStudentTrend(teacherId, studentId);
        return Result.success(trend);
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

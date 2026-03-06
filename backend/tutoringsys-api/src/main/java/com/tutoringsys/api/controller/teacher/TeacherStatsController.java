package com.tutoringsys.api.controller.teacher;

import com.tutoringsys.api.dto.AssignmentStatsVo;
import com.tutoringsys.api.dto.CourseStatsVo;
import com.tutoringsys.common.response.R;
import com.tutoringsys.service.TeacherStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/teacher/stats")
@PreAuthorize("hasRole('TEACHER')")
@Tag(name = "教师统计分析", description = "教师统计分析相关接口")
public class TeacherStatsController {

    @Resource
    private TeacherStatsService teacherStatsService;

    @GetMapping("/assignments/{assignmentId}")
    @Operation(summary = "获取作业统计数据", description = "获取指定作业的统计数据，包括平均分、最高分、最低分等")
    public R<AssignmentStatsVo> getAssignmentStats(
            @Parameter(description = "作业ID") @PathVariable Long assignmentId) {
        AssignmentStatsVo stats = teacherStatsService.getAssignmentStats(assignmentId);
        return R.ok(stats);
    }

    @GetMapping("/courses/{courseId}")
    @Operation(summary = "获取课程统计数据", description = "获取指定课程的统计数据，包括学生人数、作业完成情况等")
    public R<CourseStatsVo> getCourseStats(
            @Parameter(description = "课程ID") @PathVariable Long courseId) {
        CourseStatsVo stats = teacherStatsService.getCourseStats(courseId);
        return R.ok(stats);
    }
}
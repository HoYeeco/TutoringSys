package com.tutoring.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.common.Result;
import com.tutoring.dto.AdminCourseVO;
import com.tutoring.dto.AdminUpdateCourseRequest;
import com.tutoring.service.AdminCourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员-课程管理", description = "管理员查看和管理所有课程")
@RestController
@RequestMapping("/admin/courses")
@RequiredArgsConstructor
public class AdminCourseController {

    private final AdminCourseService adminCourseService;

    @Operation(summary = "获取课程列表", description = "分页查询所有课程，支持按教师筛选和关键字搜索")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<AdminCourseVO>> listCourses(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "教师ID筛选") @RequestParam(required = false) Long teacherId,
            @Parameter(description = "关键字搜索") @RequestParam(required = false) String keyword) {
        Page<AdminCourseVO> coursePage = adminCourseService.listCourses(page, size, teacherId, keyword);
        return Result.success(coursePage);
    }

    @Operation(summary = "获取课程详情", description = "根据ID获取课程详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<AdminCourseVO> getCourseById(@PathVariable Long id) {
        AdminCourseVO course = adminCourseService.getCourseById(id);
        if (course == null) {
            return Result.error(404, "课程不存在");
        }
        return Result.success(course);
    }

    @Operation(summary = "更新课程", description = "修改课程信息，包括更换教师")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<AdminCourseVO> updateCourse(@PathVariable Long id, @Valid @RequestBody AdminUpdateCourseRequest request) {
        try {
            AdminCourseVO course = adminCourseService.updateCourse(id, request);
            return Result.success(course);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "删除课程", description = "删除课程（需先移除关联数据）")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteCourse(@PathVariable Long id) {
        try {
            adminCourseService.deleteCourse(id);
            return Result.success(null);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}

package com.tutoring.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.common.Result;
import com.tutoring.dto.CreateCourseRequest;
import com.tutoring.dto.TeacherCourseVO;
import com.tutoring.dto.UpdateCourseRequest;
import com.tutoring.entity.User;
import com.tutoring.service.TeacherCourseService;
import com.tutoring.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "教师课程管理", description = "教师课程管理相关接口")
@RestController
@RequestMapping("/teacher/courses")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class TeacherCourseController {

    private final TeacherCourseService teacherCourseService;
    private final UserService userService;

    @Operation(summary = "获取课程列表")
    @GetMapping("/list")
    public Result<Page<TeacherCourseVO>> getCourseList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        
        Long teacherId = getCurrentUserId();
        Page<TeacherCourseVO> result = teacherCourseService.getCourseList(teacherId, page, size, keyword);
        return Result.success(result);
    }

    @Operation(summary = "获取课程详情")
    @GetMapping("/{courseId}")
    public Result<TeacherCourseVO> getCourseDetail(@PathVariable Long courseId) {
        Long teacherId = getCurrentUserId();
        TeacherCourseVO detail = teacherCourseService.getCourseDetail(teacherId, courseId);
        return Result.success(detail);
    }

    @Operation(summary = "创建课程")
    @PostMapping
    public Result<Long> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        Long teacherId = getCurrentUserId();
        Long courseId = teacherCourseService.createCourse(teacherId, request);
        return Result.success(courseId);
    }

    @Operation(summary = "更新课程")
    @PutMapping
    public Result<Void> updateCourse(@Valid @RequestBody UpdateCourseRequest request) {
        Long teacherId = getCurrentUserId();
        teacherCourseService.updateCourse(teacherId, request);
        return Result.success(null);
    }

    @Operation(summary = "删除课程")
    @DeleteMapping("/{courseId}")
    public Result<Void> deleteCourse(@PathVariable Long courseId) {
        Long teacherId = getCurrentUserId();
        teacherCourseService.deleteCourse(teacherId, courseId);
        return Result.success(null);
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

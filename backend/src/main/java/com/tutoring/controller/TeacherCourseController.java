package com.tutoring.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.common.Result;
import com.tutoring.dto.CreateCourseRequest;
import com.tutoring.dto.TeacherCourseVO;
import com.tutoring.dto.UpdateCourseRequest;
import com.tutoring.dto.UserVO;
import com.tutoring.entity.User;
import com.tutoring.service.TeacherCourseService;
import com.tutoring.service.UserService;
import com.tutoring.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
        
        Long teacherId = SecurityUtil.getCurrentUserId(userService);
        Page<TeacherCourseVO> result = teacherCourseService.getCourseList(teacherId, page, size, keyword);
        return Result.success(result);
    }

    @Operation(summary = "获取课程详情")
    @GetMapping("/{courseId}")
    public Result<TeacherCourseVO> getCourseDetail(@PathVariable Long courseId) {
        Long teacherId = SecurityUtil.getCurrentUserId(userService);
        TeacherCourseVO detail = teacherCourseService.getCourseDetail(teacherId, courseId);
        return Result.success(detail);
    }

    @Operation(summary = "创建课程")
    @PostMapping
    public Result<Long> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        Long teacherId = SecurityUtil.getCurrentUserId(userService);
        Long courseId = teacherCourseService.createCourse(teacherId, request);
        return Result.success(courseId);
    }

    @Operation(summary = "更新课程")
    @PutMapping
    public Result<Void> updateCourse(@Valid @RequestBody UpdateCourseRequest request) {
        Long teacherId = SecurityUtil.getCurrentUserId(userService);
        teacherCourseService.updateCourse(teacherId, request);
        return Result.success(null);
    }

    @Operation(summary = "删除课程")
    @DeleteMapping("/{courseId}")
    public Result<Void> deleteCourse(@PathVariable Long courseId) {
        Long teacherId = SecurityUtil.getCurrentUserId(userService);
        teacherCourseService.deleteCourse(teacherId, courseId);
        return Result.success(null);
    }

    @Operation(summary = "获取所有学生列表")
    @GetMapping("/students")
    public Result<List<UserVO>> getAllStudents() {
        List<User> students = userService.lambdaQuery()
            .eq(User::getRole, "STUDENT")
            .eq(User::getStatus, 1)
            .orderByAsc(User::getRealName)
            .list();
        
        List<UserVO> studentVOs = students.stream()
            .map(user -> UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .build())
            .collect(Collectors.toList());
        
        return Result.success(studentVOs);
    }

}

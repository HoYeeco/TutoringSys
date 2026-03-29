package com.tutoring.controller;

import com.tutoring.common.Result;
import com.tutoring.dto.CourseDetailVO;
import com.tutoring.dto.StudentCourseVO;
import com.tutoring.entity.User;
import com.tutoring.service.StudentCourseService;
import com.tutoring.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "学生课程", description = "学生课程相关接口")
@RestController
@RequestMapping("/student/courses")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class StudentCourseController {

    private final StudentCourseService studentCourseService;
    private final UserService userService;

    @Operation(summary = "获取我的课程列表")
    @GetMapping
    public Result<List<StudentCourseVO>> getMyCourses() {
        Long studentId = getCurrentUserId();
        List<StudentCourseVO> courses = studentCourseService.getMyCourses(studentId);
        return Result.success(courses);
    }

    @Operation(summary = "获取课程详情")
    @GetMapping("/{courseId}")
    public Result<CourseDetailVO> getCourseDetail(@PathVariable Long courseId) {
        Long studentId = getCurrentUserId();
        CourseDetailVO detail = studentCourseService.getCourseDetail(studentId, courseId);
        if (detail == null) {
            return Result.error(404, "课程不存在或未选课");
        }
        return Result.success(detail);
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

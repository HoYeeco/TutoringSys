package com.tutoring.controller;

import com.tutoring.common.Result;
import com.tutoring.dto.StudentDashboardVO;
import com.tutoring.entity.User;
import com.tutoring.service.StudentDashboardService;
import com.tutoring.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "学生首页", description = "学生首页相关接口")
@RestController
@RequestMapping("/student/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class StudentDashboardController {

    private final StudentDashboardService studentDashboardService;
    private final UserService userService;

    @Operation(summary = "获取首页概览数据")
    @GetMapping("/overview")
    public Result<StudentDashboardVO> getOverview() {
        Long studentId = getCurrentUserId();
        StudentDashboardVO overview = studentDashboardService.getOverview(studentId);
        return Result.success(overview);
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

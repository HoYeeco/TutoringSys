package com.tutoring.controller;

import com.tutoring.common.Result;
import com.tutoring.dto.TeacherDashboardVO;
import com.tutoring.service.TeacherDashboardService;
import com.tutoring.service.UserService;
import com.tutoring.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "教师首页", description = "教师首页相关接口")
@RestController
@RequestMapping("/teacher/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class TeacherDashboardController {

    private final TeacherDashboardService teacherDashboardService;
    private final UserService userService;

    @Operation(summary = "获取首页概览")
    @GetMapping("/overview")
    public Result<TeacherDashboardVO> getOverview() {
        Long teacherId = SecurityUtil.getCurrentUserId(userService);
        TeacherDashboardVO overview = teacherDashboardService.getDashboardOverview(teacherId);
        return Result.success(overview);
    }

}

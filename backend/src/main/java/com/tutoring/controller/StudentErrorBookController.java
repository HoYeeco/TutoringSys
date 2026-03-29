package com.tutoring.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.common.Result;
import com.tutoring.dto.ErrorBookListVO;
import com.tutoring.dto.ErrorBookStatVO;
import com.tutoring.entity.User;
import com.tutoring.service.StudentErrorBookService;
import com.tutoring.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "学生错题本", description = "学生错题本相关接口")
@RestController
@RequestMapping("/student/error-book")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class StudentErrorBookController {

    private final StudentErrorBookService studentErrorBookService;
    private final UserService userService;

    @Operation(summary = "获取错题统计（按课程）")
    @GetMapping("/stats")
    public Result<List<ErrorBookStatVO>> getErrorBookStats() {
        Long studentId = getCurrentUserId();
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
        
        Long studentId = getCurrentUserId();
        Page<ErrorBookListVO> result = studentErrorBookService.getErrorBookList(
            studentId, page, size, courseId, type, keyword);
        return Result.success(result);
    }

    @Operation(summary = "移除错题")
    @DeleteMapping("/{errorBookId}")
    public Result<Void> removeFromErrorBook(@PathVariable Long errorBookId) {
        Long studentId = getCurrentUserId();
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
        
        Long studentId = getCurrentUserId();
        studentErrorBookService.exportErrorBook(studentId, courseId, type, format, response);
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

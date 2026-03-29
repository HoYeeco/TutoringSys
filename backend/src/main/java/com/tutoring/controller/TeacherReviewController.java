package com.tutoring.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.common.Result;
import com.tutoring.dto.ReviewActionRequest;
import com.tutoring.dto.ReviewDetailVO;
import com.tutoring.dto.ReviewListVO;
import com.tutoring.entity.User;
import com.tutoring.service.TeacherReviewService;
import com.tutoring.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "教师批改复核", description = "教师批改复核相关接口")
@RestController
@RequestMapping("/teacher/review")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class TeacherReviewController {

    private final TeacherReviewService teacherReviewService;
    private final UserService userService;

    @Operation(summary = "获取待复核列表")
    @GetMapping("/list")
    public Result<Page<ReviewListVO>> getReviewList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long assignmentId,
            @RequestParam(required = false) String keyword) {
        
        Long teacherId = getCurrentUserId();
        Page<ReviewListVO> result = teacherReviewService.getReviewList(
            teacherId, page, size, courseId, assignmentId, keyword);
        return Result.success(result);
    }

    @Operation(summary = "获取复核详情")
    @GetMapping("/{answerId}")
    public Result<ReviewDetailVO> getReviewDetail(@PathVariable Long answerId) {
        Long teacherId = getCurrentUserId();
        ReviewDetailVO detail = teacherReviewService.getReviewDetail(teacherId, answerId);
        return Result.success(detail);
    }

    @Operation(summary = "采用AI评分")
    @PostMapping("/{answerId}/accept")
    public Result<Void> acceptAiScore(
            @PathVariable Long answerId,
            @RequestParam(required = false) String teacherFeedback) {
        Long teacherId = getCurrentUserId();
        teacherReviewService.acceptAiScore(teacherId, answerId, teacherFeedback);
        return Result.success(null);
    }

    @Operation(summary = "修改分数")
    @PostMapping("/{answerId}/modify")
    public Result<Void> modifyScore(
            @PathVariable Long answerId,
            @RequestParam Integer newScore,
            @RequestParam(required = false) String teacherFeedback) {
        Long teacherId = getCurrentUserId();
        teacherReviewService.modifyScore(teacherId, answerId, newScore, teacherFeedback);
        return Result.success(null);
    }

    @Operation(summary = "批量采用AI评分")
    @PostMapping("/batch/accept")
    public Result<Void> batchAccept(
            @RequestBody List<Long> answerIds,
            @RequestParam(required = false) String teacherFeedback) {
        Long teacherId = getCurrentUserId();
        teacherReviewService.batchAccept(teacherId, answerIds, teacherFeedback);
        return Result.success(null);
    }

    @Operation(summary = "批量修改分数")
    @PostMapping("/batch/modify")
    public Result<Void> batchModify(
            @RequestBody List<Long> answerIds,
            @RequestParam Integer newScore,
            @RequestParam(required = false) String teacherFeedback) {
        Long teacherId = getCurrentUserId();
        teacherReviewService.batchModify(teacherId, answerIds, newScore, teacherFeedback);
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

package com.tutoring.controller;

import com.tutoring.common.Result;
import com.tutoring.dto.GradingResult;
import com.tutoring.dto.QwenResponse;
import com.tutoring.service.GradingAsyncService;
import com.tutoring.service.QwenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "测试-AI批改", description = "测试Qwen API调用")
@RestController
@RequestMapping("/test/ai")
@RequiredArgsConstructor
public class AITestController {

    private final QwenService qwenService;
    private final GradingAsyncService gradingAsyncService;

    @Operation(summary = "测试Qwen对话", description = "测试与Qwen模型的基本对话")
    @GetMapping("/chat")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> testChat(@RequestParam String message) {
        QwenResponse response = qwenService.chat("你是一个友好的助手", message);
        if (response != null && response.isSuccess()) {
            return Result.success(qwenService.extractContent(response));
        }
        return Result.error(500, "AI调用失败: " + (response != null ? response.getMessage() : "未知错误"));
    }

    @Operation(summary = "测试AI评分", description = "测试AI对主观题的评分能力")
    @GetMapping("/grade")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<GradingResult> testGrade(
            @RequestParam String question,
            @RequestParam String reference,
            @RequestParam String answer,
            @RequestParam(defaultValue = "10") Integer maxScore) {
        GradingResult result = qwenService.gradeAnswer(question, reference, answer, maxScore);
        return Result.success(result);
    }

    @Operation(summary = "手动触发批改", description = "手动触发异步批改流程测试")
    @GetMapping("/trigger-grading")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> triggerGrading(
            @RequestParam Long submissionId,
            @RequestParam Long studentId,
            @RequestParam Long assignmentId) {
        gradingAsyncService.processGrading(submissionId, studentId, assignmentId);
        return Result.success("批改任务已触发");
    }
}

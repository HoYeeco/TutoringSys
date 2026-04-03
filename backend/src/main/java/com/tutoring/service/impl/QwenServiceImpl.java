package com.tutoring.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutoring.dto.GradingResult;
import com.tutoring.dto.QwenResponse;
import com.tutoring.service.QwenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
public class QwenServiceImpl implements QwenService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${llm.api-key}")
    private String apiKey;

    @Value("${llm.model:qwen-max}")
    private String model;

    @Value("${llm.retry:3}")
    private int maxRetries;

    private static final String QWEN_API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

    private static final String GRADING_SYSTEM_PROMPT = """
        你是一个专业的作业批改助手。请根据题目、参考答案和学生答案进行评分。

        重要规则：
        1. 如果学生答案为空、空白、或只有无意义字符（如"无"、"不知道"、"略"等），必须给 0 分，errors 写"未提交答案"
        2. 如果学生答案与题目完全不相关，errors 写"不相关答案"
        3. 如果学生答案是无效内容（如乱码、无意义字符组合），errors 写"无效答案"
        4. 错误点应该简洁描述问题类型，如："未提交答案"、"不相关答案"、"无效答案"、"内容不完整"、"理解偏差"等
        5. 不要出现"|||"或其他格式标记符号
        6. 必须始终返回 errors 和 suggestions 数组，即使学生答案得满分：
           - 如果答案完美（得满分），errors 数组必须包含一个元素："无"
           - suggestions 数组不能为空，可以提供进一步提升的建议或鼓励
        7. errors 数组和 suggestions 数组都不能为空

        请以 JSON 格式返回评分结果，格式如下：
        {
            "score": <得分，整数>,
            "errors": [<错误点 1>, <错误点 2>],
            "suggestions": ["<改进建议 1>", "<改进建议 2>"],
            "feedback": ""
        }

        评分要求：
        1. 严格按照参考答案评分
        2. 错误点要简洁（不超过 6 个字），描述问题的本质
        3. 给出具体可操作的改进建议
        4. feedback 字段保持为空字符串
        5. 满分答案示例：{"score": 10, "errors": ["无"], "suggestions": ["答案完美，继续保持！"], "feedback": ""}
        """;

    public QwenServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public QwenResponse chat(String systemPrompt, String userMessage) {
        return doChat(systemPrompt, userMessage);
    }

    @Override
    @Retryable(
        value = {RestClientException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public QwenResponse chatWithRetry(String systemPrompt, String userMessage, int maxRetries) {
        return doChat(systemPrompt, userMessage);
    }

    @Override
    public GradingResult gradeAnswer(String questionContent, String referenceAnswer, String studentAnswer, Integer maxScore) {
        System.out.println("=== DEBUG: gradeAnswer 被调用 ===");
        System.out.println("=== questionContent: " + questionContent);
        System.out.println("=== referenceAnswer: " + referenceAnswer);
        System.out.println("=== studentAnswer: " + studentAnswer);
        System.out.println("=== maxScore: " + maxScore);
        System.out.println("=== API Key 是否存在: " + (apiKey != null && !apiKey.isEmpty()));
        
        if (studentAnswer == null || studentAnswer.trim().isEmpty() || isNoAnswer(studentAnswer)) {
            System.out.println("=== DEBUG: 学生未作答 ===");
            return GradingResult.builder()
                .score(0)
                .errors(List.of("未提交答案"))
                .suggestions(List.of("建议学生认真作答，按照题目要求完成作答内容"))
                .feedback("")
                .build();
        }

        String userMessage = String.format("""
            题目：%s
            
            参考答案：%s
            
            学生答案：%s
            
            满分：%d分
            
            请对学生的答案进行评分，返回 JSON 格式的结果。
            注意：如果学生答案明显的不完整或答非所问，应酌情扣分。
            """, questionContent, referenceAnswer, studentAnswer, maxScore);

        System.out.println("=== DEBUG: 准备调用 Qwen API ===");
        log.info("调用 Qwen API: question={}, maxScore={}", questionContent.substring(0, Math.min(50, questionContent.length())), maxScore);
        QwenResponse response = chatWithRetry(GRADING_SYSTEM_PROMPT, userMessage, maxRetries);
        
        System.out.println("=== DEBUG: Qwen API 返回，code=" + (response != null ? response.getCode() : "null"));
        log.info("Qwen API 响应：code={}, message={}, isSuccess={}", 
            response != null ? response.getCode() : "null",
            response != null ? response.getMessage() : "null",
            response != null ? response.isSuccess() : "false");
        
        if (response != null && response.isSuccess()) {
            String content = extractContent(response);
            System.out.println("=== DEBUG: AI 返回的 JSON: " + content);
            log.info("AI 返回的 JSON 内容：{}", content);
            return GradingResult.parseFromJson(content);
        }

        System.out.println("=== DEBUG: AI 评分失败 ===");
        log.error("AI 评分失败，返回默认结果。response={}", response);
        return GradingResult.builder()
            .score(0)
            .errors(List.of("AI 评分失败：" + (response != null ? response.getMessage() : "未知错误")))
            .suggestions(List.of())
            .feedback("评分服务暂时不可用，请稍后重试")
            .build();
    }

    private QwenResponse doChat(String systemPrompt, String userMessage) {
        try {
            String requestBody = buildRequestBody(systemPrompt, userMessage);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                QWEN_API_URL,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                QwenResponse qwenResponse = objectMapper.readValue(response.getBody(), QwenResponse.class);
                log.debug("Qwen API response: {}", qwenResponse);
                System.out.println("=== Qwen API 原始响应: " + response.getBody() + " ===");
                return qwenResponse;
            } else {
                log.error("Qwen API request failed with status: {}, body: {}", response.getStatusCode(), response.getBody());
                return QwenResponse.builder()
                    .message("API 请求失败：" + response.getStatusCode() + " | " + response.getBody())
                    .code(String.valueOf(response.getStatusCode().value()))
                    .build();
            }
        } catch (Exception e) {
            log.error("调用 Qwen API 失败", e);
            System.out.println("=== Qwen API 异常: " + e.getMessage() + " ===");
            return QwenResponse.builder()
                .message("调用 AI 服务失败：" + e.getMessage())
                .code("ERROR")
                .build();
        }
    }

    private String buildRequestBody(String systemPrompt, String userMessage) {
        return String.format("""
            {
                "model": "%s",
                "input": {
                    "messages": [
                        {
                            "role": "system",
                            "content": "%s"
                        },
                        {
                            "role": "user",
                            "content": "%s"
                        }
                    ]
                },
                "parameters": {
                    "result_format": "json"
                }
            }
            """, model, escapeJson(systemPrompt), escapeJson(userMessage));
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    @Override
    public String extractContent(QwenResponse response) {
        try {
            if (response.getOutput() != null && response.getOutput().getChoices() != null && 
                !response.getOutput().getChoices().isEmpty()) {
                return response.getOutput().getChoices().get(0).getMessage().getContent();
            }
        } catch (Exception e) {
            log.error("提取 Qwen 响应内容失败", e);
        }
        return "";
    }

    private boolean isNoAnswer(String answer) {
        if (answer == null) return true;
        
        String trimmed = answer.trim().toLowerCase();
        return trimmed.isEmpty() || 
               trimmed.equals("无") || 
               trimmed.equals("不知道") || 
               trimmed.equals("略") ||
               trimmed.equals("暂无") ||
               trimmed.equals("不会");
    }
}

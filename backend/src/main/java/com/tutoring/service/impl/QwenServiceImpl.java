package com.tutoring.service.impl;

import com.tutoring.dto.GradingResult;
import com.tutoring.dto.QwenRequest;
import com.tutoring.dto.QwenResponse;
import com.tutoring.entity.LlmCallLog;
import com.tutoring.mapper.LlmCallLogMapper;
import com.tutoring.service.QwenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QwenServiceImpl implements QwenService {

    private final RestTemplate restTemplate;
    private final LlmCallLogMapper llmCallLogMapper;

    @Value("${llm.api-key}")
    private String apiKey;

    @Value("${llm.model}")
    private String model;

    @Value("${llm.timeout:30000}")
    private int timeout;

    @Value("${llm.retry:3}")
    private int maxRetries;

    private static final String QWEN_API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

    private static final String GRADING_SYSTEM_PROMPT = """
        你是一个专业的作业批改助手。请根据题目、参考答案和学生答案进行评分。
        
        重要规则：
        1. 如果学生答案为空、空白、或只有无意义字符（如"无"、"不知道"、"略"等），必须给0分
        2. 学生未作答时，feedback应写"学生未作答"，errors写"未提交答案"，suggestions写建议学生认真作答
        
        请以JSON格式返回评分结果，格式如下：
        {
            "score": <得分，整数>,
            "errors": [<错误点列表>],
            "suggestions": [<改进建议列表>],
            "feedback": "<详细评语>"
        }
        
        评分要求：
        1. 严格按照参考答案评分
        2. 指出学生的具体错误
        3. 给出改进建议
        4. 提供详细的评语
        """;

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
        if (studentAnswer == null || studentAnswer.trim().isEmpty() || isNoAnswer(studentAnswer)) {
            return GradingResult.builder()
                .score(0)
                .errors(java.util.List.of("学生未作答"))
                .suggestions(java.util.List.of("建议学生认真完成作业"))
                .feedback("学生未作答，得分为0分。")
                .build();
        }

        String userMessage = String.format("""
            题目：%s
            
            参考答案：%s
            
            学生答案：%s
            
            满分：%d分
            
            请对学生的答案进行评分，返回JSON格式的结果。
            注意：如果学生答案明显不完整或答非所问，应酌情扣分。
            """, questionContent, referenceAnswer, studentAnswer, maxScore);

        QwenResponse response = chatWithRetry(GRADING_SYSTEM_PROMPT, userMessage, maxRetries);
        
        if (response != null && response.isSuccess()) {
            String content = extractContent(response);
            return GradingResult.parseFromJson(content);
        }

        return GradingResult.builder()
            .score(0)
            .errors(java.util.List.of("AI评分失败: " + (response != null ? response.getMessage() : "未知错误")))
            .suggestions(java.util.List.of())
            .feedback("评分服务暂时不可用，请稍后重试")
            .build();
    }

    @Override
    public String extractContent(QwenResponse response) {
        if (response == null) {
            return null;
        }
        return response.getContent();
    }

    private boolean isNoAnswer(String answer) {
        if (answer == null) return true;
        String trimmed = answer.trim().toLowerCase();
        if (trimmed.isEmpty()) return true;
        
        String[] noAnswerKeywords = {"无", "不知道", "略", "不会", "没做", "未答", "空", "none", "n/a", "null", "未作答"};
        for (String keyword : noAnswerKeywords) {
            if (trimmed.equals(keyword)) {
                return true;
            }
        }
        
        return trimmed.length() < 3 && !trimmed.matches(".*[\\u4e00-\\u9fa5a-zA-Z0-9].*");
    }

    private QwenResponse doChat(String systemPrompt, String userMessage) {
        String requestId = "req_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        LocalDateTime requestTime = LocalDateTime.now();

        try {
            QwenRequest request = QwenRequest.createChatRequest(model, systemPrompt, userMessage);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<QwenRequest> entity = new HttpEntity<>(request, headers);

            log.debug("Calling Qwen API with model: {}, requestId: {}", model, requestId);

            ResponseEntity<String> rawResponse = restTemplate.exchange(
                QWEN_API_URL,
                HttpMethod.POST,
                entity,
                String.class
            );

            log.debug("Qwen API raw response: {}", rawResponse.getBody());

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            QwenResponse qwenResponse = mapper.readValue(rawResponse.getBody(), QwenResponse.class);
            log.info("Qwen API parsed response - code: {}, output: {}", 
                qwenResponse != null ? qwenResponse.getCode() : "null",
                qwenResponse != null ? qwenResponse.getMessage() : "null");
            log.info("Qwen API output: {}", qwenResponse != null ? qwenResponse.getOutput() : null);
            LocalDateTime responseTime = LocalDateTime.now();

            if (qwenResponse != null) {
                qwenResponse.setRequestId(requestId);
            }

            saveCallLog(requestId, null, model, request, qwenResponse, null, requestTime, responseTime);

            return qwenResponse;

        } catch (Exception e) {
            log.error("Failed to call Qwen API: {}", e.getMessage(), e);
            
            LocalDateTime responseTime = LocalDateTime.now();
            saveCallLog(requestId, null, model, null, null, e.getMessage(), requestTime, responseTime);

            return QwenResponse.builder()
                .requestId(requestId)
                .code("ERROR")
                .message(e.getMessage())
                .build();
        }
    }

    private void saveCallLog(String requestId, Long userId, String modelName,
            QwenRequest request, QwenResponse response, String errorMessage,
            LocalDateTime requestTime, LocalDateTime responseTime) {
        try {
            LlmCallLog log = new LlmCallLog();
            log.setRequestId(requestId);
            log.setUserId(userId);
            log.setModelName(modelName);
            log.setRequestTime(requestTime);
            log.setResponseTime(responseTime);
            log.setErrorMessage(errorMessage);

            if (response != null && response.isSuccess()) {
                log.setStatus(1);
                if (response.getUsage() != null) {
                    log.setPromptTokens(response.getUsage().getPromptTokens());
                    log.setCompletionTokens(response.getUsage().getCompletionTokens());
                    log.setTotalTokens(response.getUsage().getTotalTokens());
                }
            } else {
                log.setStatus(0);
            }

            llmCallLogMapper.insert(log);
        } catch (Exception e) {
            log.error("Failed to save LLM call log: {}", e.getMessage());
        }
    }
}

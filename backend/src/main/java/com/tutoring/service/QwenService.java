package com.tutoring.service;

import com.tutoring.dto.GradingResult;
import com.tutoring.dto.QwenResponse;

public interface QwenService {

    QwenResponse chat(String systemPrompt, String userMessage);

    QwenResponse chatWithRetry(String systemPrompt, String userMessage, int maxRetries);

    GradingResult gradeAnswer(String questionContent, String referenceAnswer, String studentAnswer, Integer maxScore);

    String extractContent(QwenResponse response);
}

package com.tutoringsys.llm;

import com.tutoringsys.common.annotation.SensitiveCheck;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

public interface LLMService {
    @Retry(name = "llmRetry", fallbackMethod = "defaultFeedback")
    @CircuitBreaker(name = "llmCircuit", fallbackMethod = "defaultFeedback")
    String generateFeedback(@SensitiveCheck String answer, String question);
    String defaultFeedback(String answer, String question, Exception e);
}
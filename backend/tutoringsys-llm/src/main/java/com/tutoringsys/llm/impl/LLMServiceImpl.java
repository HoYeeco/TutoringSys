package com.tutoringsys.llm.impl;

import com.tutoringsys.llm.LLMService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LLMServiceImpl implements LLMService {

    private static final Logger log = LoggerFactory.getLogger(LLMServiceImpl.class);
    
    private final Counter llmCallCounter;
    private final Timer llmCallTimer;

    public LLMServiceImpl(MeterRegistry registry) {
        this.llmCallCounter = Counter.builder("llm.calls.total")
                .description("Total number of LLM API calls")
                .tag("application", "tutoringsys")
                .register(registry);
        this.llmCallTimer = Timer.builder("llm.call.duration")
                .description("LLM API call duration")
                .register(registry);
    }

    @Override
    @Retry(name = "llmRetry", fallbackMethod = "defaultFeedback")
    @CircuitBreaker(name = "llmCircuit", fallbackMethod = "defaultFeedback")
    public String generateFeedback(String answer, String question) {
        llmCallCounter.increment();
        return llmCallTimer.record(() -> {
            // 这里调用大模型API
            // 暂时返回模拟结果
            return "这是一个模拟的反馈结果。";
        });
    }

    @Override
    public String defaultFeedback(String answer, String question, Exception e) {
        log.error("LLM调用失败", e);
        return "当前批改服务繁忙，暂无法生成详细反馈，请稍后重试。";
    }
}
package com.tutoringsys.llm.impl;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LLMServiceImplTest {

    @Mock
    private MeterRegistry meterRegistry;

    @InjectMocks
    private LLMServiceImpl llmService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateFeedback() {
        // 准备测试数据
        String answer = "测试答案";
        String question = "测试问题";

        // 执行测试
        String result = llmService.generateFeedback(answer, question);

        // 验证结果
        assertNotNull(result);
        assertEquals("这是一个模拟的反馈结果。", result);
    }

    @Test
    void testDefaultFeedback() {
        // 准备测试数据
        String answer = "测试答案";
        String question = "测试问题";
        Exception e = new RuntimeException("测试异常");

        // 执行测试
        String result = llmService.defaultFeedback(answer, question, e);

        // 验证结果
        assertNotNull(result);
        assertEquals("当前批改服务繁忙，暂无法生成详细反馈，请稍后重试。", result);
    }
}

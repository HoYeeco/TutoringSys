package com.tutoringsys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoringsys.api.dto.StudentAssignmentVo;
import com.tutoringsys.api.dto.SubmissionDto;
import com.tutoringsys.api.dto.SubmissionQuestionDto;
import com.tutoringsys.llm.LLMService;
import com.tutoringsys.service.dto.GradingReportVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentAssignmentServiceImplTest {

    @Mock
    private LLMService llmService;

    @InjectMocks
    private StudentAssignmentServiceImpl studentAssignmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAssignmentList() {
        // 准备测试数据
        int page = 1;
        int size = 10;
        String status = "pending";

        // 执行测试
        IPage<StudentAssignmentVo> result = studentAssignmentService.getAssignmentList(page, size, status);

        // 验证结果
        assertNotNull(result);
        assertEquals(page, result.getCurrent());
        assertEquals(size, result.getSize());
    }

    @Test
    void testSubmitAssignment() {
        // 准备测试数据
        SubmissionDto dto = new SubmissionDto();
        dto.setAssignmentId(1L);

        List<SubmissionQuestionDto> answers = new ArrayList<>();
        SubmissionQuestionDto answer1 = new SubmissionQuestionDto();
        answer1.setQuestionId(1L);
        answer1.setAnswerContent("A");
        answers.add(answer1);

        SubmissionQuestionDto answer2 = new SubmissionQuestionDto();
        answer2.setQuestionId(2L);
        answer2.setAnswerContent("这是一个主观题答案");
        answers.add(answer2);

        dto.setAnswers(answers);

        // 模拟LLM调用
        when(llmService.generateFeedback(anyString(), anyString())).thenReturn("这是一个模拟的反馈结果。");

        // 执行测试
        boolean result = studentAssignmentService.submitAssignment(dto);

        // 验证结果
        assertTrue(result);
        verify(llmService, times(1)).generateFeedback(anyString(), anyString());
    }

    @Test
    void testGetGradingReport() {
        // 准备测试数据
        String submissionId = "1";

        // 执行测试
        GradingReportVo result = studentAssignmentService.getGradingReport(submissionId);

        // 验证结果
        assertNotNull(result);
    }
}

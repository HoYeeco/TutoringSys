package com.tutoringsys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tutoringsys.dao.entity.Assignment;
import com.tutoringsys.dao.entity.AssignmentQuestion;
import com.tutoringsys.dao.mapper.AssignmentMapper;
import com.tutoringsys.dao.mapper.AssignmentQuestionMapper;
import com.tutoringsys.service.dto.AssignmentCreateDto;
import com.tutoringsys.service.dto.QuestionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherAssignmentServiceImplTest {

    @Mock
    private AssignmentMapper assignmentMapper;

    @Mock
    private AssignmentQuestionMapper assignmentQuestionMapper;

    @InjectMocks
    private TeacherAssignmentServiceImpl assignmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateAssignment() {
        // 准备测试数据
        AssignmentCreateDto dto = new AssignmentCreateDto();
        dto.setTitle("测试作业");
        dto.setCourseId(1L);
        dto.setDeadline("2026-03-10 23:59:59");

        List<QuestionDto> questions = new ArrayList<>();
        QuestionDto q1 = new QuestionDto();
        q1.setType("single");
        q1.setContent("测试题1");
        q1.setOptions("A|B|C|D");
        q1.setCorrectAnswer("A");
        q1.setScore(10);
        questions.add(q1);

        QuestionDto q2 = new QuestionDto();
        q2.setType("multiple");
        q2.setContent("测试题2");
        q2.setOptions("A|B|C|D");
        q2.setCorrectAnswer("A,B");
        q2.setScore(15);
        questions.add(q2);

        dto.setQuestions(questions);

        // 模拟插入作业
        Assignment assignment = new Assignment();
        assignment.setId(1L);
        when(assignmentMapper.insert(any(Assignment.class))).thenReturn(1);

        // 执行测试
        boolean result = assignmentService.createAssignment(dto, 1L);

        // 验证结果
        assertTrue(result);
        verify(assignmentMapper, times(1)).insert(any(Assignment.class));
        verify(assignmentQuestionMapper, times(2)).insert(any(AssignmentQuestion.class));
    }

    @Test
    void testPublishAssignment() {
        // 准备测试数据
        Long assignmentId = 1L;

        // 模拟作业存在
        Assignment assignment = new Assignment();
        assignment.setId(assignmentId);
        assignment.setCourseId(1L);
        when(assignmentMapper.selectById(assignmentId)).thenReturn(assignment);

        // 执行测试
        boolean result = assignmentService.publishAssignment(assignmentId, 1L);

        // 验证结果
        assertTrue(result);
        verify(assignmentMapper, times(1)).updateById(any(Assignment.class));
    }

    @Test
    void testUpdateGrading() {
        // 准备测试数据
        Long submissionId = 1L;
        Integer score = 90;
        String feedback = "测试反馈";

        // 执行测试
        boolean result = assignmentService.updateGrading(submissionId, score, feedback);

        // 验证结果
        assertTrue(result);
        // 这里应该验证调用了相关的mapper方法
    }
}

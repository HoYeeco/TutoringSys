package com.tutoringsys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoringsys.api.dto.*;
import com.tutoringsys.dao.entity.Question;
import com.tutoringsys.service.QuestionService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Service
public class QuestionServiceImpl implements QuestionService {

    @Override
    public IPage<Question> pageQuestions(int page, int size, Map<String, Object> params) {
        // 简化实现
        return new Page<>();
    }

    @Override
    public Question createQuestion(QuestionCreateDto dto) {
        // 简化实现
        return new Question();
    }

    @Override
    public Question updateQuestion(Long id, QuestionUpdateDto dto) {
        // 简化实现
        return new Question();
    }

    @Override
    public void deleteQuestion(Long id) {
        // 简化实现
    }

    @Override
    public Question getQuestionById(Long id) {
        // 简化实现
        return new Question();
    }

    @Override
    public Map<String, Object> importQuestions(MultipartFile file) {
        // 简化实现
        return Map.of();
    }

    @Override
    public void exportQuestions(HttpServletResponse response, Map<String, Object> params) {
        // 简化实现
    }
}

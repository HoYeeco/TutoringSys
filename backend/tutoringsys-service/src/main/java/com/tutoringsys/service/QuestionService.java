package com.tutoringsys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutoringsys.common.dto.*;
import com.tutoringsys.dao.entity.Question;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface QuestionService {
    IPage<Question> pageQuestions(int page, int size, Map<String, Object> params);
    Question createQuestion(QuestionCreateDto dto);
    Question updateQuestion(Long id, QuestionUpdateDto dto);
    void deleteQuestion(Long id);
    Question getQuestionById(Long id);
    Map<String, Object> importQuestions(MultipartFile file);
    void exportQuestions(HttpServletResponse response, Map<String, Object> params);
}

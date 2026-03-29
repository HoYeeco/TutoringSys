package com.tutoring.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tutoring.entity.Question;
import com.tutoring.mapper.QuestionMapper;
import com.tutoring.service.QuestionService;
import org.springframework.stereotype.Service;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {
}

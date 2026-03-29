package com.tutoring.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tutoring.entity.StudentAnswer;
import com.tutoring.mapper.StudentAnswerMapper;
import com.tutoring.service.StudentAnswerService;
import org.springframework.stereotype.Service;

@Service
public class StudentAnswerServiceImpl extends ServiceImpl<StudentAnswerMapper, StudentAnswer> implements StudentAnswerService {
}

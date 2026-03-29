package com.tutoring.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tutoring.entity.Submission;
import com.tutoring.mapper.SubmissionMapper;
import com.tutoring.service.SubmissionService;
import org.springframework.stereotype.Service;

@Service
public class SubmissionServiceImpl extends ServiceImpl<SubmissionMapper, Submission> implements SubmissionService {
}

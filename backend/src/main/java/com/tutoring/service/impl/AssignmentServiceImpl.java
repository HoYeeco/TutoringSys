package com.tutoring.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tutoring.entity.Assignment;
import com.tutoring.mapper.AssignmentMapper;
import com.tutoring.service.AssignmentService;
import org.springframework.stereotype.Service;

@Service
public class AssignmentServiceImpl extends ServiceImpl<AssignmentMapper, Assignment> implements AssignmentService {
}

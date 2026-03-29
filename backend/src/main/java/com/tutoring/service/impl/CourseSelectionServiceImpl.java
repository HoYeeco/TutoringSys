package com.tutoring.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tutoring.entity.CourseSelection;
import com.tutoring.mapper.CourseSelectionMapper;
import com.tutoring.service.CourseSelectionService;
import org.springframework.stereotype.Service;

@Service
public class CourseSelectionServiceImpl extends ServiceImpl<CourseSelectionMapper, CourseSelection> implements CourseSelectionService {
}

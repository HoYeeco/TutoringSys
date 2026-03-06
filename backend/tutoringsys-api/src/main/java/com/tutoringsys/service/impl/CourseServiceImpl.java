package com.tutoringsys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoringsys.api.dto.*;
import com.tutoringsys.dao.entity.Course;
import com.tutoringsys.service.CourseService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CourseServiceImpl implements CourseService {

    @Override
    public IPage<AdminCourseVo> pageCourses(int page, int size, Map<String, Object> params) {
        // 简化实现
        return new Page<>();
    }

    @Override
    public Course createCourse(AdminCourseCreateDto dto) {
        // 简化实现
        return new Course();
    }

    @Override
    public Course updateCourse(Long id, AdminCourseUpdateDto dto) {
        // 简化实现
        return new Course();
    }

    @Override
    public void deleteCourse(Long id) {
        // 简化实现
    }

    @Override
    public Course getCourseById(Long id) {
        // 简化实现
        return new Course();
    }
}

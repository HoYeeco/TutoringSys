package com.tutoringsys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoringsys.api.dto.CourseStudentVo;
import com.tutoringsys.service.TeacherCourseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherCourseServiceImpl implements TeacherCourseService {

    @Override
    public IPage<CourseStudentVo> getCourseStudents(Long courseId, int page, int size) {
        // 简化实现
        return new Page<>();
    }

    @Override
    public boolean addStudents(Long courseId, List<Long> studentIds) {
        // 简化实现
        return true;
    }

    @Override
    public boolean removeStudent(Long courseId, Long studentId) {
        // 简化实现
        return true;
    }
}

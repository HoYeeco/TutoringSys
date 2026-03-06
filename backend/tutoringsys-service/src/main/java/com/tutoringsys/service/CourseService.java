package com.tutoringsys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutoringsys.common.dto.*;
import com.tutoringsys.dao.entity.Course;

import java.util.Map;

public interface CourseService {
    IPage<AdminCourseVo> pageCourses(int page, int size, Map<String, Object> params);
    Course createCourse(AdminCourseCreateDto dto);
    Course updateCourse(Long id, AdminCourseUpdateDto dto);
    void deleteCourse(Long id);
    Course getCourseById(Long id);
}

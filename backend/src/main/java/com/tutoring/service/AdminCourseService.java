package com.tutoring.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.AdminCourseVO;
import com.tutoring.dto.AdminUpdateCourseRequest;

public interface AdminCourseService {

    Page<AdminCourseVO> listCourses(Integer page, Integer size, Long teacherId, String keyword);

    AdminCourseVO getCourseById(Long id);

    AdminCourseVO updateCourse(Long id, AdminUpdateCourseRequest request);

    void deleteCourse(Long id);
}

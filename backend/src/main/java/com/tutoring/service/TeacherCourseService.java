package com.tutoring.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.CreateCourseRequest;
import com.tutoring.dto.TeacherCourseVO;
import com.tutoring.dto.UpdateCourseRequest;

public interface TeacherCourseService {

    Page<TeacherCourseVO> getCourseList(Long teacherId, Integer page, Integer size, String keyword);

    TeacherCourseVO getCourseDetail(Long teacherId, Long courseId);

    Long createCourse(Long teacherId, CreateCourseRequest request);

    void updateCourse(Long teacherId, UpdateCourseRequest request);

    void deleteCourse(Long teacherId, Long courseId);

}

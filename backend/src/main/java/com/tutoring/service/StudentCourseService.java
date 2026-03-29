package com.tutoring.service;

import com.tutoring.dto.CourseDetailVO;
import com.tutoring.dto.StudentCourseVO;

import java.util.List;

public interface StudentCourseService {

    List<StudentCourseVO> getMyCourses(Long studentId);

    CourseDetailVO getCourseDetail(Long studentId, Long courseId);

}

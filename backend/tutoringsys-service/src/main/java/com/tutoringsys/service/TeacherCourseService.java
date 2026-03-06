package com.tutoringsys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutoringsys.common.dto.CourseStudentVo;

import java.util.List;

public interface TeacherCourseService {
    IPage<CourseStudentVo> getCourseStudents(Long courseId, int page, int size);
    boolean addStudents(Long courseId, List<Long> studentIds);
    boolean removeStudent(Long courseId, Long studentId);
}

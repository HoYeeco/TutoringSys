package com.tutoringsys.service;

import com.tutoringsys.api.dto.AssignmentStatsVo;
import com.tutoringsys.api.dto.CourseStatsVo;

public interface TeacherStatsService {
    AssignmentStatsVo getAssignmentStats(Long assignmentId);
    CourseStatsVo getCourseStats(Long courseId);
}

package com.tutoringsys.service;

import com.tutoringsys.common.dto.AssignmentStatsVo;
import com.tutoringsys.common.dto.CourseStatsVo;

public interface TeacherStatsService {
    AssignmentStatsVo getAssignmentStats(Long assignmentId);
    CourseStatsVo getCourseStats(Long courseId);
}

package com.tutoringsys.service.impl;

import com.tutoringsys.api.dto.AssignmentStatsVo;
import com.tutoringsys.api.dto.CourseStatsVo;
import com.tutoringsys.service.TeacherStatsService;
import org.springframework.stereotype.Service;

@Service
public class TeacherStatsServiceImpl implements TeacherStatsService {

    @Override
    public AssignmentStatsVo getAssignmentStats(Long assignmentId) {
        // 简化实现
        return new AssignmentStatsVo();
    }

    @Override
    public CourseStatsVo getCourseStats(Long courseId) {
        // 简化实现
        return new CourseStatsVo();
    }
}

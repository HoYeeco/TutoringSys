package com.tutoring.service.impl;

import com.tutoring.dto.StudentDashboardVO;
import com.tutoring.mapper.*;
import com.tutoring.service.StudentDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentDashboardServiceImpl implements StudentDashboardService {

    private final CourseSelectionMapper courseSelectionMapper;
    private final AssignmentMapper assignmentMapper;
    private final SubmissionMapper submissionMapper;

    @Override
    @Cacheable(value = "studentDashboard", key = "'overview:' + #studentId")
    public StudentDashboardVO getOverview(Long studentId) {
        log.info("从数据库查询学生仪表盘数据: studentId={}", studentId);
        
        Long selectedCourses = countSelectedCourses(studentId);
        Long pendingAssignments = countPendingAssignments(studentId);
        Long gradedSubmissions = countGradedSubmissions(studentId);
        BigDecimal averageScore = calculateAverageScore(studentId);

        return StudentDashboardVO.builder()
            .selectedCourses(selectedCourses)
            .pendingAssignments(pendingAssignments)
            .gradedSubmissions(gradedSubmissions)
            .averageScore(averageScore)
            .build();
    }

    private Long countSelectedCourses(Long studentId) {
        return courseSelectionMapper.selectCountByStudentId(studentId);
    }

    private Long countPendingAssignments(Long studentId) {
        return assignmentMapper.countPendingByStudentId(studentId);
    }

    private Long countGradedSubmissions(Long studentId) {
        return submissionMapper.countGradedByStudentId(studentId);
    }

    private BigDecimal calculateAverageScore(Long studentId) {
        BigDecimal avg = submissionMapper.selectAverageScoreByStudentId(studentId);
        if (avg == null) {
            return BigDecimal.ZERO;
        }
        return avg.setScale(2, RoundingMode.HALF_UP);
    }

}

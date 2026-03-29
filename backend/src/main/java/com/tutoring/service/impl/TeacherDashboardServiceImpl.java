package com.tutoring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutoring.dto.TeacherDashboardVO;
import com.tutoring.entity.*;
import com.tutoring.mapper.*;
import com.tutoring.service.TeacherDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherDashboardServiceImpl implements TeacherDashboardService {

    private final CourseMapper courseMapper;
    private final CourseSelectionMapper courseSelectionMapper;
    private final AssignmentMapper assignmentMapper;
    private final StudentAnswerMapper studentAnswerMapper;

    @Override
    @Cacheable(value = "teacherDashboard", key = "'overview:' + #teacherId")
    public TeacherDashboardVO getDashboardOverview(Long teacherId) {
        log.info("从数据库查询教师仪表盘数据: teacherId={}", teacherId);
        
        List<Course> courses = courseMapper.selectList(
            new LambdaQueryWrapper<Course>()
                .eq(Course::getTeacherId, teacherId)
        );

        Long courseCount = (long) courses.size();

        Long studentCount = 0L;
        if (!courses.isEmpty()) {
            List<Long> courseIds = courses.stream()
                .map(Course::getId)
                .collect(Collectors.toList());

            List<CourseSelection> selections = courseSelectionMapper.selectList(
                new LambdaQueryWrapper<CourseSelection>()
                    .in(CourseSelection::getCourseId, courseIds)
                    .select(CourseSelection::getStudentId)
            );

            studentCount = selections.stream()
                .map(CourseSelection::getStudentId)
                .distinct()
                .count();
        }

        LocalDateTime now = LocalDateTime.now();

        Long ongoingAssignmentCount = assignmentMapper.selectCount(
            new LambdaQueryWrapper<Assignment>()
                .eq(Assignment::getTeacherId, teacherId)
                .eq(Assignment::getStatus, "published")
                .gt(Assignment::getDeadline, now)
        );

        Long overdueAssignmentCount = assignmentMapper.selectCount(
            new LambdaQueryWrapper<Assignment>()
                .eq(Assignment::getTeacherId, teacherId)
                .eq(Assignment::getStatus, "published")
                .lt(Assignment::getDeadline, now)
        );

        Long pendingReviewCount = 0L;
        if (!courses.isEmpty()) {
            List<Long> courseIds = courses.stream()
                .map(Course::getId)
                .collect(Collectors.toList());

            List<Assignment> assignments = assignmentMapper.selectList(
                new LambdaQueryWrapper<Assignment>()
                    .in(Assignment::getCourseId, courseIds)
                    .eq(Assignment::getStatus, "published")
            );

            if (!assignments.isEmpty()) {
                List<Long> assignmentIds = assignments.stream()
                    .map(Assignment::getId)
                    .collect(Collectors.toList());

                pendingReviewCount = studentAnswerMapper.selectCount(
                    new LambdaQueryWrapper<StudentAnswer>()
                        .in(StudentAnswer::getAssignmentId, assignmentIds)
                        .eq(StudentAnswer::getGraderType, "AI")
                        .eq(StudentAnswer::getReviewStatus, 1)
                        .eq(StudentAnswer::getIsDraft, 0)
                );
            }
        }

        return TeacherDashboardVO.builder()
            .courseCount(courseCount)
            .studentCount(studentCount)
            .ongoingAssignmentCount(ongoingAssignmentCount)
            .pendingReviewCount(pendingReviewCount)
            .overdueAssignmentCount(overdueAssignmentCount)
            .build();
    }

}

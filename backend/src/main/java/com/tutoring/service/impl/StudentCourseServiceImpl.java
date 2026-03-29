package com.tutoring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutoring.dto.CourseDetailVO;
import com.tutoring.dto.StudentAssignmentVO;
import com.tutoring.dto.StudentCourseVO;
import com.tutoring.entity.*;
import com.tutoring.mapper.*;
import com.tutoring.service.StudentCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentCourseServiceImpl implements StudentCourseService {

    private final CourseSelectionMapper courseSelectionMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final AssignmentMapper assignmentMapper;
    private final SubmissionMapper submissionMapper;

    @Override
    public List<StudentCourseVO> getMyCourses(Long studentId) {
        List<CourseSelection> selections = courseSelectionMapper.selectList(
            new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getStudentId, studentId)
        );

        if (selections.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> courseIds = selections.stream()
            .map(CourseSelection::getCourseId)
            .collect(Collectors.toList());

        List<Course> courses = courseMapper.selectBatchIds(courseIds);

        List<Long> teacherIds = courses.stream()
            .map(Course::getTeacherId)
            .distinct()
            .collect(Collectors.toList());

        Map<Long, String> teacherNameMap = userMapper.selectBatchIds(teacherIds)
            .stream()
            .collect(Collectors.toMap(User::getId, User::getRealName));

        return courses.stream()
            .map(course -> StudentCourseVO.builder()
                .id(course.getId())
                .name(course.getName())
                .description(course.getDescription())
                .teacherId(course.getTeacherId())
                .teacherName(teacherNameMap.getOrDefault(course.getTeacherId(), "未知"))
                .createTime(course.getCreateTime())
                .build())
            .collect(Collectors.toList());
    }

    @Override
    public CourseDetailVO getCourseDetail(Long studentId, Long courseId) {
        CourseSelection selection = courseSelectionMapper.selectOne(
            new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getStudentId, studentId)
                .eq(CourseSelection::getCourseId, courseId)
        );

        if (selection == null) {
            return null;
        }

        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            return null;
        }

        User teacher = userMapper.selectById(course.getTeacherId());
        String teacherName = teacher != null ? teacher.getRealName() : "未知";

        List<Assignment> assignments = assignmentMapper.selectList(
            new LambdaQueryWrapper<Assignment>()
                .eq(Assignment::getCourseId, courseId)
                .orderByAsc(Assignment::getDeadline)
        );

        List<Long> assignmentIds = assignments.stream()
            .map(Assignment::getId)
            .collect(Collectors.toList());

        Map<Long, Submission> submissionMap = new HashMap<>();
        if (!assignmentIds.isEmpty()) {
            List<Submission> submissions = submissionMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                    .eq(Submission::getStudentId, studentId)
                    .in(Submission::getAssignmentId, assignmentIds)
            );
            submissionMap = submissions.stream()
                .collect(Collectors.toMap(Submission::getAssignmentId, s -> s, (a, b) -> a));
        }

        LocalDateTime now = LocalDateTime.now();
        Map<Long, Submission> finalSubmissionMap = submissionMap;
        List<StudentAssignmentVO> assignmentVOs = assignments.stream()
            .map(assignment -> {
                String status;
                Submission submission = finalSubmissionMap.get(assignment.getId());

                if (submission != null) {
                    status = "submitted";
                } else if (now.isAfter(assignment.getDeadline())) {
                    status = "overdue";
                } else {
                    status = "pending";
                }

                return StudentAssignmentVO.builder()
                    .id(assignment.getId())
                    .title(assignment.getTitle())
                    .description(assignment.getDescription())
                    .deadline(assignment.getDeadline())
                    .totalScore(assignment.getTotalScore())
                    .status(status)
                    .build();
            })
            .collect(Collectors.toList());

        return CourseDetailVO.builder()
            .id(course.getId())
            .name(course.getName())
            .description(course.getDescription())
            .teacherId(course.getTeacherId())
            .teacherName(teacherName)
            .createTime(course.getCreateTime())
            .assignments(assignmentVOs)
            .build();
    }

}

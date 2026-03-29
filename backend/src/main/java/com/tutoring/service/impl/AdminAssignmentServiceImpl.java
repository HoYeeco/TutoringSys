package com.tutoring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.AdminAssignmentVO;
import com.tutoring.entity.*;
import com.tutoring.mapper.*;
import com.tutoring.service.AdminAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAssignmentServiceImpl implements AdminAssignmentService {

    private final AssignmentMapper assignmentMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final QuestionMapper questionMapper;
    private final SubmissionMapper submissionMapper;
    private final CourseSelectionMapper courseSelectionMapper;

    @Override
    public Page<AdminAssignmentVO> listAssignments(Integer page, Integer size, Long courseId,
            String status, String keyword) {

        LambdaQueryWrapper<Assignment> wrapper = new LambdaQueryWrapper<>();

        if (courseId != null) {
            wrapper.eq(Assignment::getCourseId, courseId);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(Assignment::getStatus, status);
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Assignment::getTitle, keyword);
        }

        wrapper.orderByDesc(Assignment::getCreateTime);

        Page<Assignment> assignmentPage = assignmentMapper.selectPage(new Page<>(page, size), wrapper);

        List<Long> assignmentIds = assignmentPage.getRecords().stream()
            .map(Assignment::getId)
            .collect(Collectors.toList());

        List<Long> courseIds = assignmentPage.getRecords().stream()
            .map(Assignment::getCourseId)
            .distinct()
            .collect(Collectors.toList());

        List<Long> teacherIds = courseIds.isEmpty() ? List.of() :
            courseMapper.selectBatchIds(courseIds).stream()
                .map(Course::getTeacherId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Course> courseMap = courseIds.isEmpty() ? Map.of() :
            courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));

        Map<Long, User> teacherMap = teacherIds.isEmpty() ? Map.of() :
            userMapper.selectBatchIds(teacherIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        Map<Long, Integer> questionCountMap = assignmentIds.isEmpty() ? Map.of() :
            questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                    .in(Question::getAssignmentId, assignmentIds)
            ).stream().collect(Collectors.groupingBy(Question::getAssignmentId,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

        Map<Long, Integer> submissionCountMap = assignmentIds.isEmpty() ? Map.of() :
            submissionMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                    .in(Submission::getAssignmentId, assignmentIds)
            ).stream().collect(Collectors.groupingBy(Submission::getAssignmentId,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

        Map<Long, Integer> totalStudentsMap = courseIds.isEmpty() ? Map.of() :
            courseSelectionMapper.selectList(
                new LambdaQueryWrapper<CourseSelection>()
                    .in(CourseSelection::getCourseId, courseIds)
            ).stream().collect(Collectors.groupingBy(CourseSelection::getCourseId,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

        List<AdminAssignmentVO> voList = assignmentPage.getRecords().stream()
            .map(assignment -> {
                Course course = courseMap.get(assignment.getCourseId());
                User teacher = course != null ? teacherMap.get(course.getTeacherId()) : null;
                int totalStudents = totalStudentsMap.getOrDefault(assignment.getCourseId(), 0);
                int submittedCount = submissionCountMap.getOrDefault(assignment.getId(), 0);

                return AdminAssignmentVO.builder()
                    .id(assignment.getId())
                    .title(assignment.getTitle())
                    .description(assignment.getDescription())
                    .courseId(assignment.getCourseId())
                    .courseName(course != null ? course.getName() : "未知课程")
                    .teacherId(course != null ? course.getTeacherId() : null)
                    .teacherName(teacher != null ? teacher.getRealName() : "未知教师")
                    .teacherAvatar(teacher != null ? teacher.getAvatar() : null)
                    .deadline(assignment.getDeadline())
                    .totalScore(assignment.getTotalScore())
                    .status(assignment.getStatus())
                    .questionCount(questionCountMap.getOrDefault(assignment.getId(), 0))
                    .submissionCount(submissionCountMap.getOrDefault(assignment.getId(), 0))
                    .totalStudents(totalStudents)
                    .submittedCount(submittedCount)
                    .createTime(assignment.getCreateTime())
                    .updateTime(assignment.getUpdateTime())
                    .build();
            })
            .collect(Collectors.toList());

        Page<AdminAssignmentVO> resultPage = new Page<>(assignmentPage.getCurrent(), assignmentPage.getSize(), assignmentPage.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }

    @Override
    public AdminAssignmentVO getAssignmentDetail(Long id) {
        Assignment assignment = assignmentMapper.selectById(id);
        if (assignment == null) {
            return null;
        }

        Course course = courseMapper.selectById(assignment.getCourseId());
        User teacher = course != null ? userMapper.selectById(course.getTeacherId()) : null;

        Integer questionCount = questionMapper.selectCount(
            new LambdaQueryWrapper<Question>()
                .eq(Question::getAssignmentId, id)
        ).intValue();

        Integer submissionCount = submissionMapper.selectCount(
            new LambdaQueryWrapper<Submission>()
                .eq(Submission::getAssignmentId, id)
        ).intValue();

        Integer totalStudents = course != null ? courseSelectionMapper.selectCount(
            new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getCourseId, course.getId())
        ).intValue() : 0;

        return AdminAssignmentVO.builder()
            .id(assignment.getId())
            .title(assignment.getTitle())
            .description(assignment.getDescription())
            .courseId(assignment.getCourseId())
            .courseName(course != null ? course.getName() : "未知课程")
            .teacherId(course != null ? course.getTeacherId() : null)
            .teacherName(teacher != null ? teacher.getRealName() : "未知教师")
            .teacherAvatar(teacher != null ? teacher.getAvatar() : null)
            .deadline(assignment.getDeadline())
            .totalScore(assignment.getTotalScore())
            .status(assignment.getStatus())
            .questionCount(questionCount)
            .submissionCount(submissionCount)
            .totalStudents(totalStudents)
            .submittedCount(submissionCount)
            .createTime(assignment.getCreateTime())
            .updateTime(assignment.getUpdateTime())
            .build();
    }

    @Override
    public void deleteAssignment(Long id) {
        Assignment assignment = assignmentMapper.selectById(id);
        if (assignment == null) {
            throw new RuntimeException("作业不存在");
        }

        questionMapper.delete(
            new LambdaQueryWrapper<Question>()
                .eq(Question::getAssignmentId, id)
        );

        submissionMapper.delete(
            new LambdaQueryWrapper<Submission>()
                .eq(Submission::getAssignmentId, id)
        );

        assignmentMapper.deleteById(id);
    }
}

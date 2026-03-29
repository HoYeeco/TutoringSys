package com.tutoring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.ReviewDetailVO;
import com.tutoring.dto.ReviewListVO;
import com.tutoring.entity.*;
import com.tutoring.exception.BusinessException;
import com.tutoring.mapper.*;
import com.tutoring.service.TeacherReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherReviewServiceImpl implements TeacherReviewService {

    private final StudentAnswerMapper studentAnswerMapper;
    private final AssignmentMapper assignmentMapper;
    private final CourseMapper courseMapper;
    private final QuestionMapper questionMapper;
    private final UserMapper userMapper;
    private final SubmissionMapper submissionMapper;

    @Override
    public Page<ReviewListVO> getReviewList(Long teacherId, Integer page, Integer size,
            Long courseId, Long assignmentId, String keyword) {

        List<Course> courses = courseMapper.selectList(
            new LambdaQueryWrapper<Course>()
                .eq(Course::getTeacherId, teacherId)
        );

        if (courses.isEmpty()) {
            return new Page<>(page, size, 0);
        }

        List<Long> courseIds = courses.stream()
            .map(Course::getId)
            .collect(Collectors.toList());

        if (courseId != null && !courseIds.contains(courseId)) {
            return new Page<>(page, size, 0);
        }

        List<Long> filteredCourseIds = courseId != null 
            ? Collections.singletonList(courseId) 
            : courseIds;

        List<Assignment> assignments = assignmentMapper.selectList(
            new LambdaQueryWrapper<Assignment>()
                .in(Assignment::getCourseId, filteredCourseIds)
                .eq(Assignment::getStatus, "published")
        );

        if (assignments.isEmpty()) {
            return new Page<>(page, size, 0);
        }

        List<Long> assignmentIds = assignments.stream()
            .map(Assignment::getId)
            .collect(Collectors.toList());

        if (assignmentId != null && !assignmentIds.contains(assignmentId)) {
            return new Page<>(page, size, 0);
        }

        List<Long> filteredAssignmentIds = assignmentId != null 
            ? Collections.singletonList(assignmentId) 
            : assignmentIds;

        LambdaQueryWrapper<StudentAnswer> queryWrapper = new LambdaQueryWrapper<StudentAnswer>()
            .in(StudentAnswer::getAssignmentId, filteredAssignmentIds)
            .eq(StudentAnswer::getGraderType, "AI")
            .eq(StudentAnswer::getReviewStatus, 1)
            .eq(StudentAnswer::getIsDraft, 0)
            .orderByDesc(StudentAnswer::getUpdateTime);

        Page<StudentAnswer> answerPage = studentAnswerMapper.selectPage(new Page<>(page, size), queryWrapper);

        if (answerPage.getRecords().isEmpty()) {
            return new Page<>(page, size, 0);
        }

        Map<Long, Assignment> assignmentMap = assignments.stream()
            .collect(Collectors.toMap(Assignment::getId, Function.identity()));

        Map<Long, Course> courseMap = courses.stream()
            .collect(Collectors.toMap(Course::getId, Function.identity()));

        List<Long> studentIds = answerPage.getRecords().stream()
            .map(StudentAnswer::getStudentId)
            .distinct()
            .collect(Collectors.toList());

        Map<Long, User> studentMap = userMapper.selectBatchIds(studentIds)
            .stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));

        List<Long> questionIds = answerPage.getRecords().stream()
            .map(StudentAnswer::getQuestionId)
            .distinct()
            .collect(Collectors.toList());

        Map<Long, Question> questionMap = questionMapper.selectBatchIds(questionIds)
            .stream()
            .collect(Collectors.toMap(Question::getId, Function.identity()));

        List<ReviewListVO> voList = answerPage.getRecords().stream()
            .map(answer -> {
                Assignment assignment = assignmentMap.get(answer.getAssignmentId());
                Course course = assignment != null ? courseMap.get(assignment.getCourseId()) : null;
                User student = studentMap.get(answer.getStudentId());
                Question question = questionMap.get(answer.getQuestionId());

                return ReviewListVO.builder()
                    .answerId(answer.getId())
                    .studentId(answer.getStudentId())
                    .studentName(student != null ? student.getRealName() : "未知")
                    .assignmentId(answer.getAssignmentId())
                    .assignmentTitle(assignment != null ? assignment.getTitle() : "未知作业")
                    .courseId(assignment != null ? assignment.getCourseId() : null)
                    .courseName(course != null ? course.getName() : "未知课程")
                    .questionId(answer.getQuestionId())
                    .questionType(question != null ? question.getType() : "未知")
                    .questionContent(question != null ? question.getContent() : "")
                    .aiScore(answer.getAiScore())
                    .finalScore(answer.getFinalScore())
                    .reviewStatus(answer.getReviewStatus())
                    .graderType(answer.getGraderType())
                    .submitTime(answer.getSubmitTime())
                    .aiGradeTime(answer.getUpdateTime())
                    .build();
            })
            .filter(vo -> {
                if (!StringUtils.hasText(keyword)) {
                    return true;
                }
                return vo.getStudentName().contains(keyword) ||
                       vo.getAssignmentTitle().contains(keyword) ||
                       vo.getCourseName().contains(keyword);
            })
            .collect(Collectors.toList());

        Page<ReviewListVO> resultPage = new Page<>(page, size, answerPage.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }

    @Override
    public ReviewDetailVO getReviewDetail(Long teacherId, Long answerId) {
        StudentAnswer answer = studentAnswerMapper.selectById(answerId);
        if (answer == null) {
            throw new BusinessException("答案不存在");
        }

        Assignment assignment = assignmentMapper.selectById(answer.getAssignmentId());
        if (assignment == null) {
            throw new BusinessException("作业不存在");
        }

        Course course = courseMapper.selectById(assignment.getCourseId());
        if (course == null || !course.getTeacherId().equals(teacherId)) {
            throw new BusinessException("无权访问该答案");
        }

        User student = userMapper.selectById(answer.getStudentId());
        Question question = questionMapper.selectById(answer.getQuestionId());

        return ReviewDetailVO.builder()
            .answerId(answer.getId())
            .studentId(answer.getStudentId())
            .studentName(student != null ? student.getRealName() : "未知")
            .assignmentId(answer.getAssignmentId())
            .assignmentTitle(assignment.getTitle())
            .courseId(assignment.getCourseId())
            .courseName(course.getName())
            .questionId(answer.getQuestionId())
            .questionType(question != null ? question.getType() : "未知")
            .questionContent(question != null ? question.getContent() : "")
            .questionOptions(question != null ? question.getOptions() : null)
            .maxScore(question != null ? question.getScore() : 0)
            .studentAnswer(answer.getAnswer())
            .correctAnswer(question != null ? question.getAnswer() : null)
            .aiScore(answer.getAiScore())
            .aiFeedback(answer.getAiFeedback())
            .finalScore(answer.getFinalScore())
            .teacherFeedback(answer.getTeacherFeedback())
            .graderType(answer.getGraderType())
            .reviewStatus(answer.getReviewStatus())
            .submitTime(answer.getSubmitTime())
            .aiGradeTime(answer.getUpdateTime())
            .build();
    }

    @Override
    @Transactional
    public void acceptAiScore(Long teacherId, Long answerId, String teacherFeedback) {
        StudentAnswer answer = validateAndGetAnswer(teacherId, answerId);

        answer.setFinalScore(answer.getAiScore());
        answer.setTeacherFeedback(teacherFeedback);
        answer.setGraderType("AI");
        answer.setReviewStatus(2);
        answer.setReviewerId(teacherId);
        answer.setReviewTime(LocalDateTime.now());
        answer.setUpdateTime(LocalDateTime.now());
        studentAnswerMapper.updateById(answer);

        updateSubmissionTotalScore(answer.getSubmissionId());
    }

    @Override
    @Transactional
    public void modifyScore(Long teacherId, Long answerId, Integer newScore, String teacherFeedback) {
        StudentAnswer answer = validateAndGetAnswer(teacherId, answerId);

        Question question = questionMapper.selectById(answer.getQuestionId());
        if (question != null && newScore > question.getScore()) {
            throw new BusinessException("分数不能超过题目满分: " + question.getScore());
        }

        answer.setFinalScore(newScore);
        answer.setTeacherFeedback(teacherFeedback);
        answer.setGraderType("TEACHER");
        answer.setReviewStatus(2);
        answer.setReviewerId(teacherId);
        answer.setReviewTime(LocalDateTime.now());
        answer.setUpdateTime(LocalDateTime.now());
        studentAnswerMapper.updateById(answer);

        updateSubmissionTotalScore(answer.getSubmissionId());
    }

    @Override
    @Transactional
    public void batchAccept(Long teacherId, List<Long> answerIds, String teacherFeedback) {
        for (Long answerId : answerIds) {
            acceptAiScore(teacherId, answerId, teacherFeedback);
        }
    }

    @Override
    @Transactional
    public void batchModify(Long teacherId, List<Long> answerIds, Integer newScore, String teacherFeedback) {
        for (Long answerId : answerIds) {
            modifyScore(teacherId, answerId, newScore, teacherFeedback);
        }
    }

    private StudentAnswer validateAndGetAnswer(Long teacherId, Long answerId) {
        StudentAnswer answer = studentAnswerMapper.selectById(answerId);
        if (answer == null) {
            throw new BusinessException("答案不存在");
        }

        Assignment assignment = assignmentMapper.selectById(answer.getAssignmentId());
        if (assignment == null) {
            throw new BusinessException("作业不存在");
        }

        Course course = courseMapper.selectById(assignment.getCourseId());
        if (course == null || !course.getTeacherId().equals(teacherId)) {
            throw new BusinessException("无权访问该答案");
        }

        if (answer.getReviewStatus() != 1) {
            throw new BusinessException("该答案已复核");
        }

        return answer;
    }

    private void updateSubmissionTotalScore(Long submissionId) {
        if (submissionId == null) {
            return;
        }

        List<StudentAnswer> answers = studentAnswerMapper.selectList(
            new LambdaQueryWrapper<StudentAnswer>()
                .eq(StudentAnswer::getSubmissionId, submissionId)
                .eq(StudentAnswer::getIsDraft, 0)
        );

        int totalScore = answers.stream()
            .filter(a -> a.getFinalScore() != null)
            .mapToInt(StudentAnswer::getFinalScore)
            .sum();

        Submission submission = submissionMapper.selectById(submissionId);
        if (submission != null) {
            submission.setFinalTotalScore(totalScore);
            submission.setReviewTime(LocalDateTime.now());
            submissionMapper.updateById(submission);
        }
    }

}

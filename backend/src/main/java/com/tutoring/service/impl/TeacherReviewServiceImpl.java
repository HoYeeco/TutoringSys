package com.tutoring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.GradingResult;
import com.tutoring.dto.ReviewDetailVO;
import com.tutoring.dto.ReviewListVO;
import com.tutoring.entity.*;
import com.tutoring.exception.BusinessException;
import com.tutoring.mapper.*;
import com.tutoring.service.QwenService;
import com.tutoring.service.TeacherReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherReviewServiceImpl implements TeacherReviewService {

    private final StudentAnswerMapper studentAnswerMapper;
    private final AssignmentMapper assignmentMapper;
    private final CourseMapper courseMapper;
    private final QuestionMapper questionMapper;
    private final UserMapper userMapper;
    private final SubmissionMapper submissionMapper;
    private final QwenService qwenService;

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

                String questionType = question != null ? question.getType() : "未知";
                String questionTypeCategory = categorizeQuestionType(questionType);

                return ReviewListVO.builder()
                    .answerId(answer.getId())
                    .studentId(answer.getStudentId())
                    .studentName(student != null ? student.getRealName() : "未知")
                    .assignmentId(answer.getAssignmentId())
                    .assignmentTitle(assignment != null ? assignment.getTitle() : "未知作业")
                    .courseId(assignment != null ? assignment.getCourseId() : null)
                    .courseName(course != null ? course.getName() : "未知课程")
                    .questionId(answer.getQuestionId())
                    .questionType(questionType)
                    .questionTypeCategory(questionTypeCategory)
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

    private String categorizeQuestionType(String type) {
        if (type == null) {
            return "综合题";
        }
        
        String upperType = type.toUpperCase();
        switch (upperType) {
            case "SINGLE":
            case "MULTIPLE":
            case "JUDGE":
                return "客观题";
            case "SUBJECTIVE":
                return "主观题";
            default:
                return "综合题";
        }
    }

    @Override
    @Transactional
    public ReviewDetailVO regradeWithAi(Long teacherId, Long answerId) {
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

        Question question = questionMapper.selectById(answer.getQuestionId());
        if (question == null) {
            throw new BusinessException("题目不存在");
        }

        String questionContent = question.getContent();
        String referenceAnswer = question.getReferenceAnswer();
        String studentAnswerContent = answer.getAnswerContent() != null 
            ? answer.getAnswerContent() 
            : answer.getAnswer();
        int maxScore = question.getScore();

        log.info("智辅批改: answerId={}, questionId={}, maxScore={}, studentAnswer={}", 
            answerId, question.getId(), maxScore, studentAnswerContent);

        if (studentAnswerContent == null || studentAnswerContent.trim().isEmpty() || isNoAnswer(studentAnswerContent)) {
            log.info("学生未作答，直接返回0分: answerId={}", answerId);
            
            answer.setAiScore(0);
            answer.setScore(0);
            answer.setAiFeedback("学生未作答，得分为0分。");
            answer.setUpdateTime(LocalDateTime.now());
            studentAnswerMapper.updateById(answer);
            
            return getReviewDetail(teacherId, answerId);
        }

        GradingResult result = qwenService.gradeAnswer(
            questionContent, 
            referenceAnswer, 
            studentAnswerContent, 
            maxScore
        );

        int aiScore = result.getScore() != null ? result.getScore() : 0;
        aiScore = Math.min(aiScore, maxScore);
        aiScore = Math.max(aiScore, 0);

        String aiFeedback = buildAiFeedback(result);

        answer.setAiScore(aiScore);
        answer.setScore(aiScore);
        answer.setAiFeedback(aiFeedback);
        answer.setUpdateTime(LocalDateTime.now());
        studentAnswerMapper.updateById(answer);

        log.info("智辅批改完成: answerId={}, newAiScore={}", answerId, aiScore);

        return getReviewDetail(teacherId, answerId);
    }

    private boolean isNoAnswer(String answer) {
        if (answer == null) return true;
        String trimmed = answer.trim().toLowerCase();
        if (trimmed.isEmpty()) return true;
        
        String[] noAnswerKeywords = {"无", "不知道", "略", "不会", "没做", "未答", "空", "none", "n/a", "null", "未作答", "未做"};
        for (String keyword : noAnswerKeywords) {
            if (trimmed.equals(keyword)) {
                return true;
            }
        }
        
        return trimmed.length() < 2;
    }

    private String buildAiFeedback(GradingResult result) {
        StringBuilder feedback = new StringBuilder();
        
        if (result.getErrors() != null && !result.getErrors().isEmpty()) {
            feedback.append("【核心错误点】");
            for (int i = 0; i < result.getErrors().size(); i++) {
                feedback.append(result.getErrors().get(i));
                if (i < result.getErrors().size() - 1) {
                    feedback.append("|||");
                }
            }
        }
        
        if (result.getSuggestions() != null && !result.getSuggestions().isEmpty()) {
            if (feedback.length() > 0) {
                feedback.append("|||");
            }
            feedback.append("【修正建议】");
            for (int i = 0; i < result.getSuggestions().size(); i++) {
                feedback.append(result.getSuggestions().get(i));
                if (i < result.getSuggestions().size() - 1) {
                    feedback.append("；");
                }
            }
        }
        
        return feedback.toString();
    }

}

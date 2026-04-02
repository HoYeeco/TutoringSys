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
        log.info("教师获取待复核列表: teacherId={}, page={}, size={}, courseId={}, assignmentId={}", 
            teacherId, page, size, courseId, assignmentId);

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

        List<Submission> pendingSubmissions = submissionMapper.selectList(
            new LambdaQueryWrapper<Submission>()
                .in(Submission::getAssignmentId, filteredAssignmentIds)
                .and(wrapper -> wrapper
                    .eq(Submission::getReviewStatus, 1)
                    .or()
                    .in(Submission::getStatus, Arrays.asList(0, 1, 2))
                )
                .orderByDesc(Submission::getSubmitTime)
        );

        log.info("查询到待处理提交记录数: {}", pendingSubmissions.size());
        for (Submission sub : pendingSubmissions) {
            log.debug("待处理提交: id={}, assignmentId={}, studentId={}, status={}, reviewStatus={}", 
                sub.getId(), sub.getAssignmentId(), sub.getStudentId(), sub.getStatus(), sub.getReviewStatus());
        }

        if (pendingSubmissions.isEmpty()) {
            return new Page<>(page, size, 0);
        }

        List<Long> submissionIds = pendingSubmissions.stream()
            .map(Submission::getId)
            .collect(Collectors.toList());

        LambdaQueryWrapper<StudentAnswer> queryWrapper = new LambdaQueryWrapper<StudentAnswer>()
            .in(StudentAnswer::getSubmissionId, submissionIds)
            .eq(StudentAnswer::getIsDraft, 0)
            .orderByDesc(StudentAnswer::getUpdateTime);

        List<StudentAnswer> allAnswers = studentAnswerMapper.selectList(queryWrapper);
        
        log.info("查询到学生答案记录数: {}", allAnswers.size());

        if (allAnswers.isEmpty()) {
            return new Page<>(page, size, 0);
        }

        Map<Long, Assignment> assignmentMap = assignments.stream()
            .collect(Collectors.toMap(Assignment::getId, Function.identity()));

        Map<Long, Course> courseMap = courses.stream()
            .collect(Collectors.toMap(Course::getId, Function.identity()));

        Map<Long, Submission> submissionMap = pendingSubmissions.stream()
            .collect(Collectors.toMap(Submission::getId, Function.identity()));

        List<Long> studentIds = allAnswers.stream()
            .map(StudentAnswer::getStudentId)
            .distinct()
            .collect(Collectors.toList());

        Map<Long, User> studentMap = userMapper.selectBatchIds(studentIds)
            .stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));

        List<Long> questionIds = allAnswers.stream()
            .map(StudentAnswer::getQuestionId)
            .distinct()
            .collect(Collectors.toList());

        Map<Long, Question> questionMap = questionIds.isEmpty() ? Map.of()
            : questionMapper.selectBatchIds(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));

        Map<String, ReviewListVO> uniqueKeyMap = new LinkedHashMap<>();
        
        for (StudentAnswer answer : allAnswers) {
            Submission submission = submissionMap.get(answer.getSubmissionId());
            if (submission == null) continue;
            
            Assignment assignment = assignmentMap.get(answer.getAssignmentId());
            Course course = assignment != null ? courseMap.get(assignment.getCourseId()) : null;
            User student = studentMap.get(answer.getStudentId());
            Question question = questionMap.get(answer.getQuestionId());

            String questionType = question != null ? question.getType() : "未知";
            String questionTypeCategory = categorizeQuestionType(questionType);
            
            boolean isSubjectiveAnswer = isSubjectiveQuestionType(questionType);
            
            String uniqueKey = answer.getStudentId() + "_" + answer.getAssignmentId();
            
            if (!uniqueKeyMap.containsKey(uniqueKey) || isSubjectiveAnswer) {
                ReviewListVO vo = ReviewListVO.builder()
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
                    .finalScore(submission.getFinalTotalScore())
                    .reviewStatus(answer.getReviewStatus())
                    .graderType(answer.getGraderType())
                    .submitTime(submission.getSubmitTime())
                    .aiGradeTime(answer.getUpdateTime())
                    .submissionStatus(submission.getStatus())
                    .submissionReviewStatus(submission.getReviewStatus())
                    .build();
                
                uniqueKeyMap.put(uniqueKey, vo);
            }
        }

        List<ReviewListVO> voList = new ArrayList<>(uniqueKeyMap.values());
        
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.toLowerCase();
            voList = voList.stream()
                .filter(vo -> 
                    (vo.getStudentName() != null && vo.getStudentName().toLowerCase().contains(kw)) ||
                    (vo.getAssignmentTitle() != null && vo.getAssignmentTitle().toLowerCase().contains(kw)) ||
                    (vo.getCourseName() != null && vo.getCourseName().toLowerCase().contains(kw)))
                .collect(Collectors.toList());
        }

        int total = voList.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);
        
        List<ReviewListVO> pagedData = fromIndex < total 
            ? voList.subList(fromIndex, toIndex) 
            : new ArrayList<>();

        Page<ReviewListVO> resultPage = new Page<>(page, size, total);
        resultPage.setRecords(pagedData);
        
        log.info("返回复核列表: 总数={}, 当前页数据量={}", total, pagedData.size());
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
            case "JUDGMENT":
                return "客观题";
            case "SUBJECTIVE":
            case "ESSAY":
            case "ESSAY":
                return "主观题";
            default:
                return "综合题";
        }
    }

    private boolean isSubjectiveQuestionType(String type) {
        if (type == null) {
            return false;
        }
        String upperType = type.toUpperCase();
        return upperType.equals("SUBJECTIVE") || 
               upperType.equals("ESSAY") || 
               upperType.equals("ESSAY");
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
            answer.setAiFeedback("【错误点】未提交答案\n\n【改进建议】建议学生认真作答，按照题目要求完成作答内容。");
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
        
        // 错误点 - 使用 ||| 分隔多个错误，每个错误不超过6个字
        if (result.getErrors() != null && !result.getErrors().isEmpty()) {
            feedback.append("【错误点】");
            List<String> truncatedErrors = result.getErrors().stream()
                .map(e -> e.length() > 6 ? e.substring(0, 6) : e)
                .collect(Collectors.toList());
            String errorsStr = String.join("|||", truncatedErrors);
            feedback.append(errorsStr);
        }
        
        // 改进建议 - 合并为一段文本
        if (result.getSuggestions() != null && !result.getSuggestions().isEmpty()) {
            if (feedback.length() > 0) {
                feedback.append("\n\n");
            }
            feedback.append("【改进建议】");
            String suggestionsStr = String.join("；", result.getSuggestions());
            feedback.append(suggestionsStr);
        }
        
        return feedback.toString().trim();
    }

}

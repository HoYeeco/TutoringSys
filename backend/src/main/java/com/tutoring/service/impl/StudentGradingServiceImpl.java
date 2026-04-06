package com.tutoring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.AnswerDetailVO;
import com.tutoring.dto.GradingDetailVO;
import com.tutoring.dto.GradingListVO;
import com.tutoring.entity.*;
import com.tutoring.exception.BusinessException;
import com.tutoring.mapper.*;
import com.tutoring.service.StudentGradingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentGradingServiceImpl implements StudentGradingService {

    private final SubmissionMapper submissionMapper;
    private final AssignmentMapper assignmentMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final StudentAnswerMapper studentAnswerMapper;
    private final QuestionMapper questionMapper;
    private final CourseSelectionMapper courseSelectionMapper;

    @Override
    public Page<GradingListVO> getGradingList(Long studentId, Integer page, Integer size,
            Long courseId, String keyword, String sortBy, String sortOrder) {

        List<CourseSelection> selections = courseSelectionMapper.selectList(
            new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getStudentId, studentId)
        );

        if (selections.isEmpty()) {
            return new Page<>(page, size, 0);
        }

        List<Long> courseIds = selections.stream()
            .map(CourseSelection::getCourseId)
            .collect(Collectors.toList());

        if (courseId != null && !courseIds.contains(courseId)) {
            return new Page<>(page, size, 0);
        }

        if (courseId != null) {
            courseIds = Collections.singletonList(courseId);
        }

        List<Assignment> assignments = assignmentMapper.selectList(
            new LambdaQueryWrapper<Assignment>()
                .in(Assignment::getCourseId, courseIds)
                .eq(Assignment::getStatus, "published")
        );

        if (assignments.isEmpty()) {
            return new Page<>(page, size, 0);
        }

        List<Long> assignmentIds = assignments.stream()
            .map(Assignment::getId)
            .collect(Collectors.toList());

        Map<Long, Assignment> assignmentMap = assignments.stream()
            .collect(Collectors.toMap(Assignment::getId, Function.identity()));

        LambdaQueryWrapper<Submission> queryWrapper = new LambdaQueryWrapper<Submission>()
            .eq(Submission::getStudentId, studentId)
            .in(Submission::getAssignmentId, assignmentIds)
            .isNotNull(Submission::getFinalTotalScore);

        List<Submission> submissions = submissionMapper.selectList(queryWrapper);

        if (submissions.isEmpty()) {
            return new Page<>(page, size, 0);
        }

        List<Long> teacherIds = assignments.stream()
            .map(Assignment::getTeacherId)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, String> teacherNameMap = userMapper.selectBatchIds(teacherIds)
            .stream()
            .collect(Collectors.toMap(User::getId, User::getRealName));

        Map<Long, Course> courseMap = courseMapper.selectBatchIds(courseIds)
            .stream()
            .collect(Collectors.toMap(Course::getId, Function.identity()));

        List<GradingListVO> allVOs = submissions.stream()
            .map(submission -> {
                Assignment assignment = assignmentMap.get(submission.getAssignmentId());
                Course course = courseMap.get(assignment.getCourseId());

                return GradingListVO.builder()
                    .submissionId(submission.getId())
                    .assignmentId(assignment.getId())
                    .assignmentTitle(assignment.getTitle())
                    .courseId(assignment.getCourseId())
                    .courseName(course != null ? course.getName() : "未知课程")
                    .teacherId(assignment.getTeacherId())
                    .teacherName(teacherNameMap.getOrDefault(assignment.getTeacherId(), "未知"))
                    .totalScore(assignment.getTotalScore())
                    .finalTotalScore(submission.getFinalTotalScore())
                    .submitTime(submission.getSubmitTime())
                    .reviewTime(submission.getReviewTime())
                    .build();
            })
            .filter(vo -> {
                if (!StringUtils.hasText(keyword)) {
                    return true;
                }
                return vo.getAssignmentTitle().contains(keyword) ||
                       vo.getCourseName().contains(keyword);
            })
            .collect(Collectors.toList());

        if ("score".equals(sortBy)) {
            allVOs.sort((a, b) -> {
                int cmp = Comparator.comparing(GradingListVO::getFinalTotalScore, 
                    Comparator.nullsLast(Comparator.naturalOrder())).compare(a, b);
                return "desc".equalsIgnoreCase(sortOrder) ? -cmp : cmp;
            });
        } else {
            allVOs.sort((a, b) -> {
                int cmp = Comparator.comparing(GradingListVO::getReviewTime,
                    Comparator.nullsLast(Comparator.naturalOrder())).compare(a, b);
                return "desc".equalsIgnoreCase(sortOrder) ? -cmp : cmp;
            });
        }

        long total = allVOs.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, allVOs.size());

        List<GradingListVO> pageData = fromIndex < allVOs.size()
            ? allVOs.subList(fromIndex, toIndex)
            : new ArrayList<>();

        Page<GradingListVO> resultPage = new Page<>(page, size, total);
        resultPage.setRecords(pageData);
        return resultPage;
    }

    @Override
    public GradingDetailVO getGradingDetail(Long studentId, Long submissionId) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException("提交记录不存在");
        }

        if (!submission.getStudentId().equals(studentId)) {
            throw new BusinessException("无权访问该记录");
        }

        if (submission.getFinalTotalScore() == null) {
            throw new BusinessException("该作业尚未批改完成");
        }

        Assignment assignment = assignmentMapper.selectById(submission.getAssignmentId());
        Course course = courseMapper.selectById(assignment.getCourseId());
        User teacher = userMapper.selectById(assignment.getTeacherId());

        List<StudentAnswer> studentAnswers = studentAnswerMapper.selectList(
            new LambdaQueryWrapper<StudentAnswer>()
                .eq(StudentAnswer::getSubmissionId, submissionId)
        );

        List<Long> questionIds = studentAnswers.stream()
            .map(StudentAnswer::getQuestionId)
            .collect(Collectors.toList());

        Map<Long, Question> questionMap = questionMapper.selectBatchIds(questionIds)
            .stream()
            .collect(Collectors.toMap(Question::getId, Function.identity()));

        List<AnswerDetailVO> answerVOs = studentAnswers.stream()
            .map(sa -> {
                Question question = questionMap.get(sa.getQuestionId());
                if (question == null) return null;

                boolean isObjective = "SINGLE".equals(question.getType()) || 
                                      "MULTIPLE".equals(question.getType()) ||
                                      "JUDGE".equals(question.getType());

                Boolean isCorrect = null;
                if (isObjective && question.getAnswer() != null && sa.getAnswer() != null) {
                    isCorrect = question.getAnswer().equalsIgnoreCase(sa.getAnswer().trim());
                }

                return AnswerDetailVO.builder()
                    .questionId(question.getId())
                    .type(question.getType())
                    .content(question.getContent())
                    .options(question.getOptions())
                    .maxScore(question.getScore())
                    .studentAnswer(sa.getAnswer())
                    .correctAnswer(question.getAnswer())
                    .referenceAnswer(question.getReferenceAnswer())
                    .isCorrect(isCorrect)
                    .score(sa.getScore())
                    .finalScore(sa.getFinalScore())
                    .analysis(question.getAnalysis())
                    .aiFeedback(sa.getAiFeedback())
                    .teacherFeedback(sa.getTeacherFeedback())
                    .build();
            })
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(AnswerDetailVO::getQuestionId))
            .collect(Collectors.toList());

        String overallTeacherFeedback = null;
        if (submission.getReviewerId() != null) {
            overallTeacherFeedback = studentAnswers.stream()
                .filter(sa -> sa.getTeacherFeedback() != null)
                .map(StudentAnswer::getTeacherFeedback)
                .collect(Collectors.joining("; "));
        }

        return GradingDetailVO.builder()
            .submissionId(submission.getId())
            .assignmentId(assignment.getId())
            .assignmentTitle(assignment.getTitle())
            .assignmentDescription(assignment.getDescription())
            .courseId(assignment.getCourseId())
            .courseName(course != null ? course.getName() : "未知课程")
            .teacherId(assignment.getTeacherId())
            .teacherName(teacher != null ? teacher.getRealName() : "未知")
            .totalScore(assignment.getTotalScore())
            .finalTotalScore(submission.getFinalTotalScore())
            .aiTotalScore(submission.getAiTotalScore())
            .submitTime(submission.getSubmitTime())
            .reviewTime(submission.getReviewTime())
            .reviewStatus(submission.getReviewStatus())
            .teacherFeedback(overallTeacherFeedback)
            .answers(answerVOs)
            .build();
    }

    @Override
    public GradingDetailVO getGradingDetailByAssignment(Long studentId, Long assignmentId) {
        Submission submission = submissionMapper.selectOne(
            new LambdaQueryWrapper<Submission>()
                .eq(Submission::getStudentId, studentId)
                .eq(Submission::getAssignmentId, assignmentId)
        );
        
        if (submission == null) {
            throw new BusinessException("未找到该作业的提交记录");
        }

        return getGradingDetail(studentId, submission.getId());
    }

}

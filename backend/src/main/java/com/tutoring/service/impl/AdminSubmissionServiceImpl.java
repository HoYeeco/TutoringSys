package com.tutoring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.SubmissionDetailVO;
import com.tutoring.dto.SubmissionRecordVO;
import com.tutoring.entity.*;
import com.tutoring.mapper.*;
import com.tutoring.service.AdminSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminSubmissionServiceImpl implements AdminSubmissionService {

    private final SubmissionMapper submissionMapper;
    private final StudentAnswerMapper studentAnswerMapper;
    private final AssignmentMapper assignmentMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final QuestionMapper questionMapper;

    @Override
    public Page<SubmissionRecordVO> listSubmissions(Integer page, Integer size, Long courseId,
            Integer reviewStatus, LocalDateTime startTime, LocalDateTime endTime) {

        LambdaQueryWrapper<Submission> wrapper = new LambdaQueryWrapper<>();

        if (startTime != null) {
            wrapper.ge(Submission::getSubmitTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(Submission::getSubmitTime, endTime);
        }
        if (reviewStatus != null) {
            wrapper.eq(Submission::getReviewStatus, reviewStatus);
        }

        wrapper.orderByDesc(Submission::getSubmitTime);

        Page<Submission> submissionPage = submissionMapper.selectPage(new Page<>(page, size), wrapper);

        List<Long> assignmentIds = submissionPage.getRecords().stream()
            .map(Submission::getAssignmentId)
            .distinct()
            .collect(Collectors.toList());

        List<Long> studentIds = submissionPage.getRecords().stream()
            .map(Submission::getStudentId)
            .distinct()
            .collect(Collectors.toList());

        Map<Long, Assignment> assignmentMap = assignmentIds.isEmpty() ? Map.of() :
            assignmentMapper.selectList(
                new LambdaQueryWrapper<Assignment>()
                    .in(Assignment::getId, assignmentIds)
            ).stream().collect(Collectors.toMap(Assignment::getId, Function.identity()));

        List<Long> courseIds = assignmentMap.values().stream()
            .map(Assignment::getCourseId)
            .distinct()
            .collect(Collectors.toList());

        Map<Long, Course> courseMap = courseIds.isEmpty() ? Map.of() :
            courseMapper.selectList(
                new LambdaQueryWrapper<Course>()
                    .in(Course::getId, courseIds)
            ).stream().collect(Collectors.toMap(Course::getId, Function.identity()));

        Map<Long, User> studentMap = studentIds.isEmpty() ? Map.of() :
            userMapper.selectList(
                new LambdaQueryWrapper<User>()
                    .in(User::getId, studentIds)
            ).stream().collect(Collectors.toMap(User::getId, Function.identity()));

        List<Long> teacherIds = courseMap.values().stream()
            .map(Course::getTeacherId)
            .distinct()
            .collect(Collectors.toList());

        Map<Long, User> teacherMap = teacherIds.isEmpty() ? Map.of() :
            userMapper.selectList(
                new LambdaQueryWrapper<User>()
                    .in(User::getId, teacherIds)
            ).stream().collect(Collectors.toMap(User::getId, Function.identity()));

        List<SubmissionRecordVO> voList = submissionPage.getRecords().stream()
            .filter(sub -> {
                if (courseId == null) return true;
                Assignment assignment = assignmentMap.get(sub.getAssignmentId());
                if (assignment == null) return false;
                return courseId.equals(assignment.getCourseId());
            })
            .map(sub -> {
                Assignment assignment = assignmentMap.get(sub.getAssignmentId());
                Course course = assignment != null ? courseMap.get(assignment.getCourseId()) : null;
                User student = studentMap.get(sub.getStudentId());
                User teacher = course != null ? teacherMap.get(course.getTeacherId()) : null;

                return SubmissionRecordVO.builder()
                    .id(sub.getId())
                    .submissionId(sub.getSubmissionId())
                    .studentId(sub.getStudentId())
                    .studentName(student != null ? student.getRealName() : "未知学生")
                    .studentUsername(student != null ? student.getUsername() : "")
                    .assignmentId(sub.getAssignmentId())
                    .assignmentTitle(assignment != null ? assignment.getTitle() : "未知作业")
                    .courseId(course != null ? course.getId() : null)
                    .courseName(course != null ? course.getName() : "未知课程")
                    .teacherId(course != null ? course.getTeacherId() : null)
                    .teacherName(teacher != null ? teacher.getRealName() : "未知教师")
                    .status(sub.getStatus())
                    .totalScore(sub.getTotalScore())
                    .aiTotalScore(sub.getAiTotalScore())
                    .finalTotalScore(sub.getFinalTotalScore())
                    .reviewStatus(sub.getReviewStatus())
                    .submitTime(sub.getSubmitTime())
                    .reviewTime(sub.getReviewTime())
                    .build();
            })
            .collect(Collectors.toList());

        Page<SubmissionRecordVO> voPage = new Page<>(submissionPage.getCurrent(), submissionPage.getSize(), submissionPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public SubmissionDetailVO getSubmissionDetail(Long id) {
        Submission submission = submissionMapper.selectById(id);
        if (submission == null) {
            return null;
        }

        Assignment assignment = assignmentMapper.selectById(submission.getAssignmentId());
        Course course = assignment != null ? courseMapper.selectById(assignment.getCourseId()) : null;
        User student = userMapper.selectById(submission.getStudentId());

        List<StudentAnswer> answers = studentAnswerMapper.selectList(
            new LambdaQueryWrapper<StudentAnswer>()
                .eq(StudentAnswer::getSubmissionId, id)
                .eq(StudentAnswer::getIsDraft, 0)
        );

        List<Long> questionIds = answers.stream()
            .map(StudentAnswer::getQuestionId)
            .distinct()
            .collect(Collectors.toList());

        Map<Long, Question> questionMap = questionIds.isEmpty() ? Map.of() :
            questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                    .in(Question::getId, questionIds)
            ).stream().collect(Collectors.toMap(Question::getId, Function.identity()));

        List<SubmissionDetailVO.AnswerDetail> answerDetails = answers.stream()
            .map(answer -> {
                Question question = questionMap.get(answer.getQuestionId());
                return SubmissionDetailVO.AnswerDetail.builder()
                    .id(answer.getId())
                    .questionId(answer.getQuestionId())
                    .questionType(question != null ? question.getType() : "")
                    .questionContent(question != null ? question.getContent() : "")
                    .questionScore(question != null ? question.getScore() : 0)
                    .answer(answer.getAnswer())
                    .answerContent(answer.getAnswerContent())
                    .score(answer.getScore())
                    .aiScore(answer.getAiScore())
                    .finalScore(answer.getFinalScore())
                    .aiFeedback(answer.getAiFeedback())
                    .teacherFeedback(answer.getTeacherFeedback())
                    .graderType(answer.getGraderType())
                    .reviewStatus(answer.getReviewStatus())
                    .reviewTime(answer.getReviewTime())
                    .build();
            })
            .collect(Collectors.toList());

        return SubmissionDetailVO.builder()
            .id(submission.getId())
            .submissionId(submission.getSubmissionId())
            .studentId(submission.getStudentId())
            .studentName(student != null ? student.getRealName() : "未知学生")
            .studentUsername(student != null ? student.getUsername() : "")
            .assignmentId(submission.getAssignmentId())
            .assignmentTitle(assignment != null ? assignment.getTitle() : "未知作业")
            .courseId(course != null ? course.getId() : null)
            .courseName(course != null ? course.getName() : "未知课程")
            .totalScore(submission.getTotalScore())
            .aiTotalScore(submission.getAiTotalScore())
            .finalTotalScore(submission.getFinalTotalScore())
            .reviewStatus(submission.getReviewStatus())
            .submitTime(submission.getSubmitTime())
            .reviewTime(submission.getReviewTime())
            .answers(answerDetails)
            .build();
    }
}

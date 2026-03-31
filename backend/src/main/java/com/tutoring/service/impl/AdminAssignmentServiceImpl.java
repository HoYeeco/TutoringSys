package com.tutoring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.AdminAssignmentVO;
import com.tutoring.dto.CreateAssignmentRequest;
import com.tutoring.dto.SubmissionDetailVO;
import com.tutoring.dto.SubmissionRecordVO;
import com.tutoring.dto.UpdateAssignmentRequest;
import com.tutoring.dto.QuestionDTO;
import com.tutoring.dto.QuestionDetailDTO;
import com.tutoring.entity.*;
import com.tutoring.mapper.*;
import com.tutoring.service.AdminAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAssignmentServiceImpl implements AdminAssignmentService {

    private final AssignmentMapper assignmentMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final QuestionMapper questionMapper;
    private final SubmissionMapper submissionMapper;
    private final CourseSelectionMapper courseSelectionMapper;
    private final StudentAnswerMapper studentAnswerMapper;

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

        List<Question> questions = questionMapper.selectList(
            new LambdaQueryWrapper<Question>()
                .eq(Question::getAssignmentId, id)
                .orderByAsc(Question::getSortOrder)
        );

        List<QuestionDetailDTO> questionDTOs = questions.stream()
            .map(q -> QuestionDetailDTO.builder()
                .id(q.getId())
                .type(q.getType())
                .content(q.getContent())
                .options(q.getOptions())
                .answer(q.getAnswer())
                .referenceAnswer(q.getReferenceAnswer())
                .score(q.getScore())
                .minWords(q.getMinWords())
                .maxWords(q.getMaxWords())
                .sortOrder(q.getSortOrder())
                .build())
            .collect(Collectors.toList());

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
            .questions(questionDTOs)
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

    @Override
    @Transactional
    public Long createAssignment(CreateAssignmentRequest request) {
        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }

        Assignment assignment = new Assignment();
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setCourseId(request.getCourseId());
        assignment.setTeacherId(course.getTeacherId());
        assignment.setDeadline(request.getDeadline());
        assignment.setTotalScore(request.getTotalScore() != null ? request.getTotalScore() : 100);
        assignment.setStatus("published");
        assignmentMapper.insert(assignment);

        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            int sortOrder = 1;
            for (QuestionDTO qdto : request.getQuestions()) {
                Question question = new Question();
                question.setAssignmentId(assignment.getId());
                question.setType(qdto.getType());
                question.setContent(qdto.getContent());
                question.setOptions(qdto.getOptions());
                question.setAnswer(qdto.getAnswer());
                question.setReferenceAnswer(qdto.getReferenceAnswer());
                question.setScore(qdto.getScore());
                question.setMinWords(qdto.getMinWords());
                question.setMaxWords(qdto.getMaxWords());
                question.setSortOrder(sortOrder++);
                questionMapper.insert(question);
            }
        }

        return assignment.getId();
    }

    @Override
    @Transactional
    public void updateAssignment(Long id, UpdateAssignmentRequest request) {
        log.info("更新作业: id={}, request={}", id, request);
        Assignment assignment = assignmentMapper.selectById(id);
        if (assignment == null) {
            throw new RuntimeException("作业不存在");
        }

        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        log.info("设置截止时间: 原值={}, 新值={}", assignment.getDeadline(), request.getDeadline());
        assignment.setDeadline(request.getDeadline());
        if (request.getTotalScore() != null) {
            assignment.setTotalScore(request.getTotalScore());
        }
        assignmentMapper.updateById(assignment);
        log.info("作业更新完成: id={}, deadline={}", assignment.getId(), assignment.getDeadline());

        questionMapper.delete(
            new LambdaQueryWrapper<Question>()
                .eq(Question::getAssignmentId, id)
        );

        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            int sortOrder = 1;
            for (QuestionDTO qdto : request.getQuestions()) {
                Question question = new Question();
                question.setAssignmentId(assignment.getId());
                question.setType(qdto.getType());
                question.setContent(qdto.getContent());
                question.setOptions(qdto.getOptions());
                question.setAnswer(qdto.getAnswer());
                question.setReferenceAnswer(qdto.getReferenceAnswer());
                question.setScore(qdto.getScore());
                question.setMinWords(qdto.getMinWords());
                question.setMaxWords(qdto.getMaxWords());
                question.setSortOrder(sortOrder++);
                questionMapper.insert(question);
            }
        }
    }

    @Override
    @Transactional
    public void deleteSubmission(Long submissionId) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new RuntimeException("提交记录不存在");
        }

        studentAnswerMapper.delete(
            new LambdaQueryWrapper<StudentAnswer>()
                .eq(StudentAnswer::getSubmissionId, submissionId)
        );

        submissionMapper.deleteById(submissionId);
    }

    @Override
    public Page<SubmissionRecordVO> getAssignmentSubmissions(Long assignmentId, Integer page, Integer size) {
        Assignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            throw new RuntimeException("作业不存在");
        }

        Page<Submission> submissionPage = submissionMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<Submission>()
                .eq(Submission::getAssignmentId, assignmentId)
                .orderByDesc(Submission::getSubmitTime)
        );

        if (submissionPage.getRecords().isEmpty()) {
            return new Page<>(page, size, 0);
        }

        List<Long> studentIds = submissionPage.getRecords().stream()
            .map(Submission::getStudentId)
            .distinct()
            .collect(Collectors.toList());

        Map<Long, User> userMap = userMapper.selectBatchIds(studentIds).stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));

        Course course = courseMapper.selectById(assignment.getCourseId());

        List<SubmissionRecordVO> voList = submissionPage.getRecords().stream()
            .map(submission -> {
                User user = userMap.get(submission.getStudentId());
                return SubmissionRecordVO.builder()
                    .id(submission.getId())
                    .submissionId(submission.getSubmissionId())
                    .studentId(submission.getStudentId())
                    .studentName(user != null ? user.getRealName() : "未知学生")
                    .studentUsername(user != null ? user.getUsername() : "")
                    .assignmentId(assignmentId)
                    .assignmentTitle(assignment.getTitle())
                    .courseId(assignment.getCourseId())
                    .courseName(course != null ? course.getName() : "未知课程")
                    .teacherId(assignment.getTeacherId())
                    .status(submission.getStatus())
                    .totalScore(submission.getFinalTotalScore())
                    .aiTotalScore(submission.getAiTotalScore())
                    .finalTotalScore(submission.getFinalTotalScore())
                    .reviewStatus(submission.getReviewStatus())
                    .submitTime(submission.getSubmitTime())
                    .reviewTime(submission.getReviewTime())
                    .build();
            })
            .collect(Collectors.toList());

        Page<SubmissionRecordVO> resultPage = new Page<>(page, size, submissionPage.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }

    @Override
    public SubmissionDetailVO getSubmissionDetail(Long submissionId) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            return null;
        }

        Assignment assignment = assignmentMapper.selectById(submission.getAssignmentId());
        Course course = assignment != null ? courseMapper.selectById(assignment.getCourseId()) : null;
        User student = userMapper.selectById(submission.getStudentId());

        List<StudentAnswer> answers = studentAnswerMapper.selectList(
            new LambdaQueryWrapper<StudentAnswer>()
                .eq(StudentAnswer::getSubmissionId, submissionId)
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
            .assignmentId(submission.getAssignmentId())
            .assignmentTitle(assignment != null ? assignment.getTitle() : "")
            .courseId(assignment != null ? assignment.getCourseId() : null)
            .courseName(course != null ? course.getName() : "")
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

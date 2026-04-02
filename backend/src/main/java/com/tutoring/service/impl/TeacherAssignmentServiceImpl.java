package com.tutoring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.*;
import com.tutoring.entity.*;
import com.tutoring.exception.BusinessException;
import com.tutoring.mapper.*;
import com.tutoring.service.MessageService;
import com.tutoring.service.TeacherAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherAssignmentServiceImpl implements TeacherAssignmentService {

    private final AssignmentMapper assignmentMapper;
    private final QuestionMapper questionMapper;
    private final CourseMapper courseMapper;
    private final SubmissionMapper submissionMapper;
    private final CourseSelectionMapper courseSelectionMapper;
    private final UserMessageMapper userMessageMapper;
    private final UserMapper userMapper;
    private final StudentAnswerMapper studentAnswerMapper;
    private final MessageService messageService;

    @Override
    @Cacheable(value = "teacherAssignments", key = "'list:' + #teacherId + ':page:' + #page + ':size:' + #size + ':status:' + (#status ?: '') + ':course:' + (#courseId ?: '') + ':keyword:' + (#keyword ?: '')", unless = "#result == null || #result.records.isEmpty()")
    public Page<TeacherAssignmentVO> getAssignmentList(Long teacherId, Integer page, Integer size,
            String status, Long courseId, String keyword) {
        log.info("从数据库查询教师作业列表: teacherId={}", teacherId);

        LambdaQueryWrapper<Assignment> queryWrapper = new LambdaQueryWrapper<Assignment>()
                .eq(Assignment::getTeacherId, teacherId);

        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Assignment::getStatus, status);
        }

        if (courseId != null) {
            queryWrapper.eq(Assignment::getCourseId, courseId);
        }

        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(Assignment::getTitle, keyword);
        }

        queryWrapper.orderByDesc(Assignment::getCreateTime);

        Page<Assignment> assignmentPage = assignmentMapper.selectPage(new Page<>(page, size), queryWrapper);

        List<Long> assignmentIds = assignmentPage.getRecords().stream()
                .map(Assignment::getId)
                .collect(Collectors.toList());

        List<Long> courseIds = assignmentPage.getRecords().stream()
                .map(Assignment::getCourseId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Course> courseMap = courseIds.isEmpty() ? new HashMap<>()
                : courseMapper.selectBatchIds(courseIds).stream()
                        .collect(Collectors.toMap(Course::getId, Function.identity()));

        Map<Long, Integer> questionCountMap = new HashMap<>();
        Map<Long, Integer> submissionCountMap = new HashMap<>();
        Map<Long, Integer> gradedCountMap = new HashMap<>();
        Map<Long, Integer> totalStudentsMap = new HashMap<>();

        if (!assignmentIds.isEmpty()) {
            List<Question> questions = questionMapper.selectList(
                    new LambdaQueryWrapper<Question>()
                            .in(Question::getAssignmentId, assignmentIds));

            questionCountMap = questions.stream()
                    .collect(Collectors.groupingBy(Question::getAssignmentId,
                            Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

            List<Submission> submissions = submissionMapper.selectList(
                    new LambdaQueryWrapper<Submission>()
                            .in(Submission::getAssignmentId, assignmentIds));

            submissionCountMap = submissions.stream()
                    .collect(Collectors.groupingBy(Submission::getAssignmentId,
                            Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

            gradedCountMap = submissions.stream()
                    .filter(s -> s.getFinalTotalScore() != null)
                    .collect(Collectors.groupingBy(Submission::getAssignmentId,
                            Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
        }

        if (!courseIds.isEmpty()) {
            List<CourseSelection> selections = courseSelectionMapper.selectList(
                    new LambdaQueryWrapper<CourseSelection>()
                            .in(CourseSelection::getCourseId, courseIds));

            totalStudentsMap = selections.stream()
                    .collect(Collectors.groupingBy(CourseSelection::getCourseId,
                            Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
        }

        Map<Long, Integer> finalQuestionCountMap = questionCountMap;
        Map<Long, Integer> finalSubmissionCountMap = submissionCountMap;
        Map<Long, Integer> finalGradedCountMap = gradedCountMap;
        Map<Long, Integer> finalTotalStudentsMap = totalStudentsMap;

        List<TeacherAssignmentVO> voList = assignmentPage.getRecords().stream()
                .map(assignment -> {
                    Course course = courseMap.get(assignment.getCourseId());
                    return TeacherAssignmentVO.builder()
                            .id(assignment.getId())
                            .title(assignment.getTitle())
                            .description(assignment.getDescription())
                            .courseId(assignment.getCourseId())
                            .courseName(course != null ? course.getName() : "未知课程")
                            .teacherId(assignment.getTeacherId())
                            .deadline(assignment.getDeadline())
                            .totalScore(assignment.getTotalScore())
                            .status(assignment.getStatus())
                            .questionCount(finalQuestionCountMap.getOrDefault(assignment.getId(), 0))
                            .submissionCount(finalSubmissionCountMap.getOrDefault(assignment.getId(), 0))
                            .gradedCount(finalGradedCountMap.getOrDefault(assignment.getId(), 0))
                            .totalStudents(finalTotalStudentsMap.getOrDefault(assignment.getCourseId(), 0))
                            .createTime(assignment.getCreateTime())
                            .updateTime(assignment.getUpdateTime())
                            .build();
                })
                .collect(Collectors.toList());

        Page<TeacherAssignmentVO> resultPage = new Page<>(page, size, assignmentPage.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }

    @Override
    @Cacheable(value = "teacherAssignments", key = "'detail:' + #teacherId + ':' + #assignmentId")
    public TeacherAssignmentVO getAssignmentDetail(Long teacherId, Long assignmentId) {
        log.info("从数据库查询作业详情: teacherId={}, assignmentId={}", teacherId, assignmentId);

        Assignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null || !assignment.getTeacherId().equals(teacherId)) {
            return null;
        }

        Course course = courseMapper.selectById(assignment.getCourseId());

        List<Question> questions = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getAssignmentId, assignmentId)
                        .orderByAsc(Question::getSortOrder));

        List<QuestionDTO> questionDTOs = questions.stream()
                .map(q -> QuestionDTO.builder()
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

        Integer submissionCount = submissionMapper.selectCount(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getAssignmentId, assignmentId))
                .intValue();

        Integer gradedCount = submissionMapper.selectCount(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getAssignmentId, assignmentId)
                        .isNotNull(Submission::getFinalTotalScore))
                .intValue();

        return TeacherAssignmentVO.builder()
                .id(assignment.getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .courseId(assignment.getCourseId())
                .courseName(course != null ? course.getName() : "未知课程")
                .teacherId(assignment.getTeacherId())
                .deadline(assignment.getDeadline())
                .totalScore(assignment.getTotalScore())
                .status(assignment.getStatus())
                .questionCount(questions.size())
                .submissionCount(submissionCount)
                .gradedCount(gradedCount)
                .createTime(assignment.getCreateTime())
                .updateTime(assignment.getUpdateTime())
                .questions(questionDTOs)
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = { "teacherAssignments", "teacherDashboard" }, allEntries = true)
    public Long createAssignment(Long teacherId, CreateAssignmentRequest request) {
        log.info("创建作业，清除缓存: teacherId={}", teacherId);

        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null || !course.getTeacherId().equals(teacherId)) {
            throw new BusinessException("课程不存在或无权访问");
        }

        Assignment assignment = new Assignment();
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setCourseId(request.getCourseId());
        assignment.setTeacherId(teacherId);
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
    @CacheEvict(value = { "teacherAssignments", "studentAssignments", "teacherDashboard",
            "studentDashboard" }, allEntries = true)
    public void updateAssignment(Long teacherId, UpdateAssignmentRequest request) {
        log.info("更新作业，清除缓存: assignmentId={}", request.getId());

        Assignment assignment = assignmentMapper.selectById(request.getId());
        if (assignment == null || !assignment.getTeacherId().equals(teacherId)) {
            throw new BusinessException("作业不存在或无权访问");
        }

        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setDeadline(request.getDeadline());
        if (request.getTotalScore() != null) {
            assignment.setTotalScore(request.getTotalScore());
        }
        assignmentMapper.updateById(assignment);

        questionMapper.delete(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getAssignmentId, request.getId()));

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

        if ("published".equals(assignment.getStatus())) {
            notifyStudentsAboutUpdate(assignment);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { "teacherAssignments", "studentAssignments", "teacherDashboard",
            "studentDashboard" }, allEntries = true)
    public void publishAssignment(Long teacherId, Long assignmentId) {
        log.info("发布作业，清除缓存: assignmentId={}", assignmentId);

        Assignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null || !assignment.getTeacherId().equals(teacherId)) {
            throw new BusinessException("作业不存在或无权访问");
        }

        if ("published".equals(assignment.getStatus())) {
            throw new BusinessException("作业已发布");
        }

        assignment.setStatus("published");
        assignmentMapper.updateById(assignment);

        notifyStudentsAboutNewAssignment(assignment);
    }

    @Override
    @Transactional
    @CacheEvict(value = { "teacherAssignments", "studentAssignments", "teacherDashboard",
            "studentDashboard" }, allEntries = true)
    public void deleteAssignment(Long teacherId, Long assignmentId) {
        log.info("删除作业，清除缓存: assignmentId={}", assignmentId);

        Assignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null || !assignment.getTeacherId().equals(teacherId)) {
            throw new BusinessException("作业不存在或无权访问");
        }

        if ("published".equals(assignment.getStatus())) {
            notifyStudentsAboutDeletion(assignment);
        }

        questionMapper.delete(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getAssignmentId, assignmentId));

        assignmentMapper.deleteById(assignmentId);
    }

    private void notifyStudentsAboutNewAssignment(Assignment assignment) {
        List<CourseSelection> selections = courseSelectionMapper.selectList(
                new LambdaQueryWrapper<CourseSelection>()
                        .eq(CourseSelection::getCourseId, assignment.getCourseId()));

        if (selections.isEmpty()) {
            return;
        }

        List<Long> studentIds = selections.stream()
                .map(CourseSelection::getStudentId)
                .collect(Collectors.toList());

        Message message = new Message();
        message.setTitle("新作业通知");
        message.setContent("您有一份新的作业《" + assignment.getTitle() + "》已发布，请及时完成。");
        message.setType("assignment");
        message.setRelatedId(assignment.getId());
        message.setRelatedType("assignment");
        messageService.save(message);

        for (Long studentId : studentIds) {
            UserMessage um = new UserMessage();
            um.setMessageId(message.getId());
            um.setUserId(studentId);
            um.setIsRead(0);
            userMessageMapper.insert(um);
        }
    }

    private void notifyStudentsAboutUpdate(Assignment assignment) {
        List<CourseSelection> selections = courseSelectionMapper.selectList(
                new LambdaQueryWrapper<CourseSelection>()
                        .eq(CourseSelection::getCourseId, assignment.getCourseId()));

        if (selections.isEmpty()) {
            return;
        }

        List<Long> studentIds = selections.stream()
                .map(CourseSelection::getStudentId)
                .collect(Collectors.toList());

        Message message = new Message();
        message.setTitle("作业更新通知");
        message.setContent("作业《" + assignment.getTitle() + "》已更新，请查看最新内容。");
        message.setType("assignment");
        message.setRelatedId(assignment.getId());
        message.setRelatedType("assignment");
        messageService.save(message);

        for (Long studentId : studentIds) {
            UserMessage um = new UserMessage();
            um.setMessageId(message.getId());
            um.setUserId(studentId);
            um.setIsRead(0);
            userMessageMapper.insert(um);
        }
    }

    private void notifyStudentsAboutDeletion(Assignment assignment) {
        List<CourseSelection> selections = courseSelectionMapper.selectList(
                new LambdaQueryWrapper<CourseSelection>()
                        .eq(CourseSelection::getCourseId, assignment.getCourseId()));

        if (selections.isEmpty()) {
            return;
        }

        List<Long> studentIds = selections.stream()
                .map(CourseSelection::getStudentId)
                .collect(Collectors.toList());

        Message message = new Message();
        message.setTitle("作业删除通知");
        message.setContent("作业《" + assignment.getTitle() + "》已被删除。");
        message.setType("SYSTEM");
        messageService.save(message);

        for (Long studentId : studentIds) {
            UserMessage um = new UserMessage();
            um.setMessageId(message.getId());
            um.setUserId(studentId);
            um.setIsRead(0);
            userMessageMapper.insert(um);
        }
    }

    @Override
    public Page<SubmissionRecordVO> getAssignmentSubmissions(Long teacherId, Long assignmentId, Integer page,
            Integer size) {
        // 验证作业是否存在且属于该教师
        Assignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null || !assignment.getTeacherId().equals(teacherId)) {
            throw new BusinessException("作业不存在或无权访问");
        }

        // 查询提交列表
        Page<Submission> submissionPage = submissionMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getAssignmentId, assignmentId)
                        .orderByDesc(Submission::getSubmitTime));

        if (submissionPage.getRecords().isEmpty()) {
            return new Page<>(page, size, 0);
        }

        // 获取学生信息
        List<Long> studentIds = submissionPage.getRecords().stream()
                .map(Submission::getStudentId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = userMapper.selectBatchIds(studentIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // 获取课程信息
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
                            .studentAvatar(user != null ? user.getAvatar() : null)
                            .assignmentId(assignmentId)
                            .assignmentTitle(assignment.getTitle())
                            .courseId(assignment.getCourseId())
                            .courseName(course != null ? course.getName() : "未知课程")
                            .teacherId(teacherId)
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
    public SubmissionDetailVO getSubmissionDetail(Long teacherId, Long submissionId) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            return null;
        }

        // 验证作业是否属于该教师
        Assignment assignment = assignmentMapper.selectById(submission.getAssignmentId());
        if (assignment == null || !assignment.getTeacherId().equals(teacherId)) {
            return null;
        }

        Course course = courseMapper.selectById(assignment.getCourseId());
        User student = userMapper.selectById(submission.getStudentId());

        List<StudentAnswer> answers = studentAnswerMapper.selectList(
                new LambdaQueryWrapper<StudentAnswer>()
                        .eq(StudentAnswer::getSubmissionId, submissionId)
                        .eq(StudentAnswer::getIsDraft, 0));

        List<Long> questionIds = answers.stream()
                .map(StudentAnswer::getQuestionId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Question> questionMap = questionIds.isEmpty() ? Map.of()
                : questionMapper.selectList(
                        new LambdaQueryWrapper<Question>()
                                .in(Question::getId, questionIds))
                        .stream().collect(Collectors.toMap(Question::getId, Function.identity()));

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
                .studentAvatar(student != null ? student.getAvatar() : null)
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

    @Override
    @Transactional
    public void reviewSubmission(Long teacherId, Long submissionId, ReviewSubmissionRequest request) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException("提交记录不存在");
        }

        Assignment assignment = assignmentMapper.selectById(submission.getAssignmentId());
        if (assignment == null || !assignment.getTeacherId().equals(teacherId)) {
            throw new BusinessException("无权访问该提交记录");
        }

        List<StudentAnswer> allAnswers = studentAnswerMapper.selectList(
            new LambdaQueryWrapper<StudentAnswer>()
                .eq(StudentAnswer::getSubmissionId, submissionId)
                .eq(StudentAnswer::getIsDraft, 0));

        Map<Long, StudentAnswer> answerMap = allAnswers.stream()
            .collect(Collectors.toMap(StudentAnswer::getQuestionId, Function.identity(), (a, b) -> a));

        LocalDateTime now = LocalDateTime.now();
        int computedTotalScore = 0;
        boolean allReviewed = true;

        if (request.getQuestions() != null) {
            for (ReviewSubmissionRequest.QuestionReview questionReview : request.getQuestions()) {
                StudentAnswer answer = answerMap.get(questionReview.getQuestionId());
                if (answer == null) {
                    log.warn("批改时未找到题目答案: questionId={}", questionReview.getQuestionId());
                    continue;
                }

                Integer newScore = questionReview.getScore();
                String feedback = questionReview.getFeedback();

                Question question = questionMapper.selectById(answer.getQuestionId());
                int maxScore = question != null ? question.getScore() : 100;
                if (newScore != null && newScore > maxScore) {
                    throw new BusinessException("第" + (question != null ? question.getSortOrder() : "") + "题分数超过满分: " + maxScore);
                }

                String originalGraderType = answer.getGraderType();
                Integer originalAiScore = answer.getAiScore();

                if ("AI".equals(originalGraderType) && originalAiScore != null && originalAiScore.equals(newScore)) {
                    answer.setFinalScore(newScore);
                    answer.setTeacherFeedback(feedback);
                    answer.setGraderType("AI");
                    answer.setReviewStatus(2);
                } else if ("AUTO".equals(originalGraderType)) {
                    answer.setScore(newScore);
                    answer.setFinalScore(newScore);
                    answer.setTeacherFeedback(feedback);
                    answer.setGraderType("TEACHER");
                    answer.setReviewStatus(2);
                } else {
                    answer.setScore(newScore);
                    answer.setFinalScore(newScore);
                    answer.setTeacherFeedback(feedback);
                    answer.setGraderType("TEACHER");
                    answer.setReviewStatus(2);
                }

                answer.setReviewerId(teacherId);
                answer.setReviewTime(now);
                studentAnswerMapper.updateById(answer);

                if (newScore != null) {
                    computedTotalScore += newScore;
                }
            }
        }

        for (StudentAnswer ans : allAnswers) {
            if (ans.getReviewStatus() == null || ans.getReviewStatus() == 1) {
                allReviewed = false;
                break;
            }
        }

        submission.setFinalTotalScore(computedTotalScore);
        submission.setReviewStatus(allReviewed ? 2 : 1);
        submission.setReviewerId(teacherId);
        submission.setReviewTime(now);
        submissionMapper.updateById(submission);

        log.info("教师批改完成: submissionId={}, finalTotalScore={}, reviewStatus={}, allReviewed={}", 
            submissionId, computedTotalScore, submission.getReviewStatus(), allReviewed);
    }

    @Override
    public AssignmentSubmissionStatusVO getAssignmentSubmissionStatus(Long teacherId, Long assignmentId) {
        // 验证作业是否存在且属于该教师
        Assignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null || !assignment.getTeacherId().equals(teacherId)) {
            throw new BusinessException("作业不存在或无权访问");
        }

        // 获取课程下的所有学生
        List<CourseSelection> selections = courseSelectionMapper.selectList(
                new LambdaQueryWrapper<CourseSelection>()
                        .eq(CourseSelection::getCourseId, assignment.getCourseId()));

        List<Long> studentIds = selections.stream()
                .map(CourseSelection::getStudentId)
                .distinct()
                .collect(Collectors.toList());

        if (studentIds.isEmpty()) {
            return AssignmentSubmissionStatusVO.builder()
                    .assignmentId(assignmentId)
                    .assignmentTitle(assignment.getTitle())
                    .totalStudents(0)
                    .submittedCount(0)
                    .notSubmittedCount(0)
                    .submittedStudents(List.of())
                    .notSubmittedStudents(List.of())
                    .allStudents(List.of())
                    .build();
        }

        // 获取学生信息
        Map<Long, User> userMap = userMapper.selectBatchIds(studentIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // 获取已提交的学生
        List<Submission> submissions = submissionMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getAssignmentId, assignmentId)
                        .in(Submission::getStudentId, studentIds));

        Set<Long> submittedStudentIds = submissions.stream()
                .map(Submission::getStudentId)
                .collect(Collectors.toSet());

        // 构建已提交学生列表
        List<AssignmentSubmissionStatusVO.StudentInfo> submittedStudents = submissions.stream()
                .map(sub -> {
                    User user = userMap.get(sub.getStudentId());
                    return AssignmentSubmissionStatusVO.StudentInfo.builder()
                            .studentId(sub.getStudentId())
                            .studentName(user != null ? user.getRealName() : "未知学生")
                            .username(user != null ? user.getUsername() : "")
                            .avatar(user != null ? user.getAvatar() : "")
                            .email(user != null ? user.getEmail() : "")
                            .phone(user != null ? user.getPhone() : "")
                            .build();
                })
                .collect(Collectors.toList());

        // 构建未提交学生列表
        List<AssignmentSubmissionStatusVO.StudentInfo> notSubmittedStudents = studentIds.stream()
                .filter(id -> !submittedStudentIds.contains(id))
                .map(id -> {
                    User user = userMap.get(id);
                    return AssignmentSubmissionStatusVO.StudentInfo.builder()
                            .studentId(id)
                            .studentName(user != null ? user.getRealName() : "未知学生")
                            .username(user != null ? user.getUsername() : "")
                            .avatar(user != null ? user.getAvatar() : "")
                            .email(user != null ? user.getEmail() : "")
                            .phone(user != null ? user.getPhone() : "")
                            .build();
                })
                .collect(Collectors.toList());

        // 构建所有学生列表
        List<AssignmentSubmissionStatusVO.StudentInfo> allStudents = studentIds.stream()
                .map(id -> {
                    User user = userMap.get(id);
                    return AssignmentSubmissionStatusVO.StudentInfo.builder()
                            .studentId(id)
                            .studentName(user != null ? user.getRealName() : "未知学生")
                            .username(user != null ? user.getUsername() : "")
                            .avatar(user != null ? user.getAvatar() : "")
                            .email(user != null ? user.getEmail() : "")
                            .phone(user != null ? user.getPhone() : "")
                            .build();
                })
                .collect(Collectors.toList());

        return AssignmentSubmissionStatusVO.builder()
                .assignmentId(assignmentId)
                .assignmentTitle(assignment.getTitle())
                .totalStudents(studentIds.size())
                .submittedCount(submittedStudents.size())
                .notSubmittedCount(notSubmittedStudents.size())
                .submittedStudents(submittedStudents)
                .notSubmittedStudents(notSubmittedStudents)
                .allStudents(allStudents)
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = "teacherAssignments", allEntries = true)
    public void deleteSubmission(Long teacherId, Long submissionId) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException("提交记录不存在");
        }

        Assignment assignment = assignmentMapper.selectById(submission.getAssignmentId());
        if (assignment == null || !assignment.getTeacherId().equals(teacherId)) {
            throw new BusinessException("无权删除此提交记录");
        }

        studentAnswerMapper.delete(
                new LambdaQueryWrapper<StudentAnswer>()
                        .eq(StudentAnswer::getSubmissionId, submissionId));

        submissionMapper.deleteById(submissionId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "teacherAssignments", allEntries = true)
    public void rejectSubmission(Long teacherId, Long submissionId) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException("提交记录不存在");
        }

        Assignment assignment = assignmentMapper.selectById(submission.getAssignmentId());
        if (assignment == null || !assignment.getTeacherId().equals(teacherId)) {
            throw new BusinessException("无权操作此提交记录");
        }

        // 将学生答案标记为草稿状态，保留原提交数据
        studentAnswerMapper.updateStatusToDraft(submissionId);

        // 删除提交记录，让学生可以重新提交
        submissionMapper.deleteById(submissionId);

        // 发送消息通知学生
        Message message = new Message();
        message.setTitle("作业打回重做");
        message.setContent(String.format("您的作业「%s」已被教师打回，请重新提交。", assignment.getTitle()));
        message.setType("system");
        messageService.sendMessage(message, List.of(submission.getStudentId()));

        log.info("作业已打回重做: submissionId={}, studentId={}, assignmentId={}", 
            submissionId, submission.getStudentId(), submission.getAssignmentId());
    }

}

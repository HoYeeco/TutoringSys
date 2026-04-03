package com.tutoring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.*;
import com.tutoring.entity.*;
import com.tutoring.event.SubmissionEvent;
import com.tutoring.exception.BusinessException;
import com.tutoring.mapper.*;
import com.tutoring.service.StudentAssignmentService;
import com.tutoring.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentAssignmentServiceImpl implements StudentAssignmentService {

    private final CourseSelectionMapper courseSelectionMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final AssignmentMapper assignmentMapper;
    private final SubmissionMapper submissionMapper;
    private final QuestionMapper questionMapper;
    private final StudentAnswerMapper studentAnswerMapper;
    private final SubmissionService submissionService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Page<StudentAssignmentListVO> getAssignmentList(Long studentId, Integer page, Integer size,
            String status, Long courseId, String keyword, String sortBy, String sortOrder) {
        log.info("从数据库查询学生作业列表: studentId={}", studentId);

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

        List<Course> courses = courseMapper.selectBatchIds(courseIds);
        Map<Long, Course> courseMap = courses.stream()
            .collect(Collectors.toMap(Course::getId, c -> c));

        List<Long> teacherIds = courses.stream()
            .map(Course::getTeacherId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
        final Map<Long, String> teacherNameMap;
        if (!teacherIds.isEmpty()) {
            teacherNameMap = userMapper.selectBatchIds(teacherIds)
                .stream()
                .collect(Collectors.toMap(User::getId, User::getRealName));
        } else {
            teacherNameMap = new HashMap<>();
        }

        List<Long> searchCourseIds = courseIds;
        if (StringUtils.hasText(keyword)) {
            List<Long> matchedCourseIds = courseMapper.selectList(
                new LambdaQueryWrapper<Course>()
                    .in(Course::getId, courseIds)
                    .like(Course::getName, keyword)
            ).stream().map(Course::getId).collect(Collectors.toList());
            
            searchCourseIds = matchedCourseIds;
        }

        if (searchCourseIds.isEmpty()) {
            return new Page<>(page, size, 0);
        }

        final List<Long> finalSearchCourseIds = searchCourseIds;
        LambdaQueryWrapper<Assignment> queryWrapper = new LambdaQueryWrapper<Assignment>()
            .in(Assignment::getCourseId, finalSearchCourseIds)
            .eq(Assignment::getStatus, "published");

        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(Assignment::getTitle, keyword);
        }

        if ("publishTime".equals(sortBy) || "createTime".equals(sortBy)) {
            if ("desc".equalsIgnoreCase(sortOrder)) {
                queryWrapper.orderByDesc(Assignment::getCreateTime);
            } else {
                queryWrapper.orderByAsc(Assignment::getCreateTime);
            }
        } else {
            if ("desc".equalsIgnoreCase(sortOrder)) {
                queryWrapper.orderByDesc(Assignment::getDeadline);
            } else {
                queryWrapper.orderByAsc(Assignment::getDeadline);
            }
        }

        List<Assignment> allAssignments = assignmentMapper.selectList(queryWrapper);

        List<Long> assignmentIds = allAssignments.stream()
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

        List<StudentAssignmentListVO> allVOs = allAssignments.stream()
            .map(assignment -> {
                Course course = courseMap.get(assignment.getCourseId());
                Submission submission = finalSubmissionMap.get(assignment.getId());

                String assignmentStatus = calculateStatus(assignment, submission, now);

                return StudentAssignmentListVO.builder()
                    .id(assignment.getId())
                    .title(assignment.getTitle())
                    .description(assignment.getDescription())
                    .courseId(assignment.getCourseId())
                    .courseName(course != null ? course.getName() : "未知课程")
                    .teacherId(assignment.getTeacherId())
                    .teacherName(teacherNameMap.getOrDefault(assignment.getTeacherId(), "未知"))
                    .deadline(assignment.getDeadline())
                    .totalScore(assignment.getTotalScore())
                    .status(assignmentStatus)
                    .finalScore(submission != null ? submission.getFinalTotalScore() : null)
                    .submitTime(submission != null ? submission.getSubmitTime() : null)
                    .build();
            })
            .filter(vo -> {
                if (!StringUtils.hasText(status)) {
                    return true;
                }
                return status.equals(vo.getStatus());
            })
            .collect(Collectors.toList());

        long total = allVOs.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, allVOs.size());

        List<StudentAssignmentListVO> pageData = fromIndex < allVOs.size()
            ? allVOs.subList(fromIndex, toIndex)
            : new ArrayList<>();

        Page<StudentAssignmentListVO> resultPage = new Page<>(page, size, total);
        resultPage.setRecords(pageData);
        return resultPage;
    }

    private String calculateStatus(Assignment assignment, Submission submission, LocalDateTime now) {
        if (submission != null) {
            Integer status = submission.getStatus();
            
            if (status != null && status == 4) {
                return "overdue"; // 失败
            }
            
            if (status != null && status == 2) {
                return "submitted"; // 批改中
            }
            
            if (status != null && status == 3) {
                // 批改完成
                Integer reviewStatus = submission.getReviewStatus();
                if (reviewStatus != null && reviewStatus == 1) {
                    return "submitted"; // 有主观题，待教师复核
                }
                return "graded"; // 全客观题已批改 或 教师已复核完成
            }
            
            return "submitted"; // 已提交(status=0或1)
        }

        if (now.isAfter(assignment.getDeadline())) {
            return "overdue";
        }

        return "pending";
    }

    @Override
    public AssignmentDetailVO getAssignmentDetail(Long studentId, Long assignmentId) {
        log.info("从数据库查询作业详情: studentId={}, assignmentId={}", studentId, assignmentId);
        
        Assignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            throw new BusinessException("作业不存在");
        }

        CourseSelection selection = courseSelectionMapper.selectOne(
            new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getStudentId, studentId)
                .eq(CourseSelection::getCourseId, assignment.getCourseId())
        );
        if (selection == null) {
            throw new BusinessException("无权访问该作业");
        }

        Course course = courseMapper.selectById(assignment.getCourseId());
        User teacher = userMapper.selectById(assignment.getTeacherId());

        List<Question> questions = questionMapper.selectList(
            new LambdaQueryWrapper<Question>()
                .eq(Question::getAssignmentId, assignmentId)
                .orderByAsc(Question::getSortOrder)
        );

        List<StudentAnswer> draftAnswers = studentAnswerMapper.selectList(
            new LambdaQueryWrapper<StudentAnswer>()
                .eq(StudentAnswer::getStudentId, studentId)
                .eq(StudentAnswer::getAssignmentId, assignmentId)
                .eq(StudentAnswer::getIsDraft, 1)
        );

        Map<Long, StudentAnswer> draftMap = draftAnswers.stream()
            .collect(Collectors.toMap(StudentAnswer::getQuestionId, Function.identity(), (a, b) -> a));

        LocalDateTime draftSaveTime = null;
        if (!draftAnswers.isEmpty()) {
            draftSaveTime = draftAnswers.stream()
                .map(StudentAnswer::getUpdateTime)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        }

        List<QuestionVO> questionVOs = questions.stream()
            .map(q -> {
                StudentAnswer draft = draftMap.get(q.getId());
                return QuestionVO.builder()
                    .id(q.getId())
                    .type(q.getType())
                    .content(q.getContent())
                    .options(q.getOptions())
                    .score(q.getScore())
                    .minWords(q.getMinWords())
                    .maxWords(q.getMaxWords())
                    .sortOrder(q.getSortOrder())
                    .studentAnswer(draft != null ? draft.getAnswer() : null)
                    .answerId(draft != null ? draft.getId() : null)
                    .build();
            })
            .collect(Collectors.toList());

        Submission submission = submissionMapper.selectOne(
            new LambdaQueryWrapper<Submission>()
                .eq(Submission::getStudentId, studentId)
                .eq(Submission::getAssignmentId, assignmentId)
        );

        String status = calculateStatus(assignment, submission, LocalDateTime.now());

        return AssignmentDetailVO.builder()
            .id(assignment.getId())
            .title(assignment.getTitle())
            .description(assignment.getDescription())
            .courseId(assignment.getCourseId())
            .courseName(course != null ? course.getName() : "未知课程")
            .teacherId(assignment.getTeacherId())
            .teacherName(teacher != null ? teacher.getRealName() : "未知")
            .deadline(assignment.getDeadline())
            .totalScore(assignment.getTotalScore())
            .status(status)
            .questions(questionVOs)
            .hasDraft(!draftAnswers.isEmpty())
            .draftSaveTime(draftSaveTime)
            .build();
    }

    @Override
    @Transactional
    public void saveDraft(Long studentId, Long assignmentId, SaveDraftRequest request) {
        Assignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            throw new BusinessException("作业不存在");
        }

        CourseSelection selection = courseSelectionMapper.selectOne(
            new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getStudentId, studentId)
                .eq(CourseSelection::getCourseId, assignment.getCourseId())
        );
        if (selection == null) {
            throw new BusinessException("无权访问该作业");
        }

        Submission existingSubmission = submissionMapper.selectOne(
            new LambdaQueryWrapper<Submission>()
                .eq(Submission::getStudentId, studentId)
                .eq(Submission::getAssignmentId, assignmentId)
        );
        if (existingSubmission != null) {
            throw new BusinessException("作业已提交，无法保存草稿");
        }

        for (SaveDraftRequest.AnswerItem item : request.getAnswers()) {
            Question question = questionMapper.selectById(item.getQuestionId());
            if (question == null || !question.getAssignmentId().equals(assignmentId)) {
                continue;
            }

            StudentAnswer existingDraft = studentAnswerMapper.selectOne(
                new LambdaQueryWrapper<StudentAnswer>()
                    .eq(StudentAnswer::getStudentId, studentId)
                    .eq(StudentAnswer::getAssignmentId, assignmentId)
                    .eq(StudentAnswer::getQuestionId, item.getQuestionId())
                    .eq(StudentAnswer::getIsDraft, 1)
            );

            if (existingDraft != null) {
                existingDraft.setAnswer(item.getAnswer());
                existingDraft.setUpdateTime(LocalDateTime.now());
                studentAnswerMapper.updateById(existingDraft);
            } else {
                StudentAnswer newDraft = new StudentAnswer();
                newDraft.setStudentId(studentId);
                newDraft.setAssignmentId(assignmentId);
                newDraft.setQuestionId(item.getQuestionId());
                newDraft.setAnswer(item.getAnswer());
                newDraft.setIsDraft(1);
                newDraft.setStatus(0);
                studentAnswerMapper.insert(newDraft);
            }
        }
    }

    @Override
    public Object getDraft(Long studentId, Long assignmentId) {
        List<StudentAnswer> draftAnswers = studentAnswerMapper.selectList(
            new LambdaQueryWrapper<StudentAnswer>()
                .eq(StudentAnswer::getStudentId, studentId)
                .eq(StudentAnswer::getAssignmentId, assignmentId)
                .eq(StudentAnswer::getIsDraft, 1)
        );

        if (draftAnswers.isEmpty()) {
            return null;
        }

        List<Map<String, Object>> answers = draftAnswers.stream()
            .map(draft -> {
                Map<String, Object> answer = new HashMap<>();
                answer.put("questionId", draft.getQuestionId());
                answer.put("answerContent", draft.getAnswer());
                return answer;
            })
            .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("answers", answers);
        return result;
    }

    @Override
    @Transactional
    @CacheEvict(value = "teacherAssignments", allEntries = true)
    public SubmitResponse submitAssignment(Long studentId, Long assignmentId, SubmitRequest request) {
        log.info("提交作业: studentId={}, assignmentId={}", studentId, assignmentId);
        
        Assignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            throw new BusinessException("作业不存在");
        }

        CourseSelection selection = courseSelectionMapper.selectOne(
            new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getStudentId, studentId)
                .eq(CourseSelection::getCourseId, assignment.getCourseId())
        );
        if (selection == null) {
            throw new BusinessException("无权访问该作业");
        }

        Submission existingSubmission = submissionMapper.selectOne(
            new LambdaQueryWrapper<Submission>()
                .eq(Submission::getStudentId, studentId)
                .eq(Submission::getAssignmentId, assignmentId)
        );
        if (existingSubmission != null) {
            throw new BusinessException("作业已提交，请勿重复提交");
        }

        List<Question> questions = questionMapper.selectList(
            new LambdaQueryWrapper<Question>()
                .eq(Question::getAssignmentId, assignmentId)
        );

        log.info("作业 {} 查询到 {} 道题目", assignmentId, questions.size());
        log.info("前端提交了 {} 道答案", request.getAnswers().size());

        Map<Long, Question> questionMap = questions.stream()
            .collect(Collectors.toMap(Question::getId, Function.identity()));

        // 检查答案数量是否匹配
        if (request.getAnswers().size() != questions.size()) {
            log.error("答案数量不匹配! 期望: {}, 实际: {}", questions.size(), request.getAnswers().size());
            Set<Long> submittedIds = request.getAnswers().stream()
                .map(SubmitRequest.AnswerItem::getQuestionId)
                .collect(Collectors.toSet());
            Set<Long> expectedIds = questions.stream()
                .map(Question::getId)
                .collect(Collectors.toSet());
            log.error("期望的题目IDs: {}", expectedIds);
            log.error("提交的题目IDs: {}", submittedIds);
            throw new BusinessException("题目数量不匹配，请刷新页面重试");
        }

        for (SubmitRequest.AnswerItem item : request.getAnswers()) {
            if (!questionMap.containsKey(item.getQuestionId())) {
                throw new BusinessException("题目ID无效: " + item.getQuestionId());
            }
            if (!StringUtils.hasText(item.getAnswer())) {
                Question q = questionMap.get(item.getQuestionId());
                throw new BusinessException("第 " + q.getSortOrder() + " 题未完成作答，请完成后再提交");
            }
        }

        Set<Long> answeredQuestionIds = request.getAnswers().stream()
            .map(SubmitRequest.AnswerItem::getQuestionId)
            .collect(Collectors.toSet());
        
        for (Question question : questions) {
            if (!answeredQuestionIds.contains(question.getId())) {
                throw new BusinessException("第 " + question.getSortOrder() + " 题未完成作答，请完成后再提交");
            }
        }

        String submissionIdStr = generateSubmissionId(studentId, assignmentId);
        Submission submission = new Submission();
        submission.setSubmissionId(submissionIdStr);
        submission.setStudentId(studentId);
        submission.setAssignmentId(assignmentId);
        submission.setStatus(0);
        submission.setSubmitTime(LocalDateTime.now());
        submissionService.save(submission);

        for (SubmitRequest.AnswerItem item : request.getAnswers()) {
            StudentAnswer existingDraft = studentAnswerMapper.selectOne(
                new LambdaQueryWrapper<StudentAnswer>()
                    .eq(StudentAnswer::getStudentId, studentId)
                    .eq(StudentAnswer::getAssignmentId, assignmentId)
                    .eq(StudentAnswer::getQuestionId, item.getQuestionId())
                    .eq(StudentAnswer::getIsDraft, 1)
            );

            if (existingDraft != null) {
                existingDraft.setSubmissionId(submission.getId());
                existingDraft.setAnswer(item.getAnswer());
                existingDraft.setIsDraft(0);
                existingDraft.setStatus(1);
                existingDraft.setSubmitTime(LocalDateTime.now());
                existingDraft.setUpdateTime(LocalDateTime.now());
                existingDraft.setGraderType("PENDING");
                existingDraft.setReviewStatus(1);
                studentAnswerMapper.updateById(existingDraft);
            } else {
                StudentAnswer newAnswer = new StudentAnswer();
                newAnswer.setSubmissionId(submission.getId());
                newAnswer.setStudentId(studentId);
                newAnswer.setAssignmentId(assignmentId);
                newAnswer.setQuestionId(item.getQuestionId());
                newAnswer.setAnswer(item.getAnswer());
                newAnswer.setIsDraft(0);
                newAnswer.setStatus(1);
                newAnswer.setSubmitTime(LocalDateTime.now());
                newAnswer.setGraderType("PENDING");
                newAnswer.setReviewStatus(1);
                studentAnswerMapper.insert(newAnswer);
            }
        }

        eventPublisher.publishEvent(new SubmissionEvent(submission.getId(), studentId, assignmentId));

        return SubmitResponse.builder()
            .submissionId(submission.getId())
            .message("作业提交成功，正在批改中...")
            .status("submitted")
            .build();
    }

    private String generateSubmissionId(Long studentId, Long assignmentId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "SUB" + timestamp + "S" + studentId + "A" + assignmentId;
    }

}

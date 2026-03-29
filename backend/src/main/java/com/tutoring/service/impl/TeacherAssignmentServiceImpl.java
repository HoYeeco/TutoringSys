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

        Map<Long, Course> courseMap = courseIds.isEmpty() ? new HashMap<>() :
            courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));

        Map<Long, Integer> questionCountMap = new HashMap<>();
        Map<Long, Integer> submissionCountMap = new HashMap<>();
        Map<Long, Integer> gradedCountMap = new HashMap<>();

        if (!assignmentIds.isEmpty()) {
            List<Question> questions = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                    .in(Question::getAssignmentId, assignmentIds)
            );

            questionCountMap = questions.stream()
                .collect(Collectors.groupingBy(Question::getAssignmentId, 
                    Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

            List<Submission> submissions = submissionMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                    .in(Submission::getAssignmentId, assignmentIds)
            );

            submissionCountMap = submissions.stream()
                .collect(Collectors.groupingBy(Submission::getAssignmentId, 
                    Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

            gradedCountMap = submissions.stream()
                .filter(s -> s.getFinalTotalScore() != null)
                .collect(Collectors.groupingBy(Submission::getAssignmentId, 
                    Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
        }

        Map<Long, Integer> finalQuestionCountMap = questionCountMap;
        Map<Long, Integer> finalSubmissionCountMap = submissionCountMap;
        Map<Long, Integer> finalGradedCountMap = gradedCountMap;

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
                .orderByAsc(Question::getSortOrder)
        );

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
                .eq(Submission::getAssignmentId, assignmentId)
        ).intValue();

        Integer gradedCount = submissionMapper.selectCount(
            new LambdaQueryWrapper<Submission>()
                .eq(Submission::getAssignmentId, assignmentId)
                .isNotNull(Submission::getFinalTotalScore)
        ).intValue();

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
    @CacheEvict(value = {"teacherAssignments", "teacherDashboard"}, allEntries = true)
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
    @CacheEvict(value = {"teacherAssignments", "studentAssignments", "teacherDashboard", "studentDashboard"}, allEntries = true)
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
                .eq(Question::getAssignmentId, request.getId())
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

        if ("published".equals(assignment.getStatus())) {
            notifyStudentsAboutUpdate(assignment);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"teacherAssignments", "studentAssignments", "teacherDashboard", "studentDashboard"}, allEntries = true)
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
    @CacheEvict(value = {"teacherAssignments", "studentAssignments", "teacherDashboard", "studentDashboard"}, allEntries = true)
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
                .eq(Question::getAssignmentId, assignmentId)
        );

        assignmentMapper.deleteById(assignmentId);
    }

    private void notifyStudentsAboutNewAssignment(Assignment assignment) {
        List<CourseSelection> selections = courseSelectionMapper.selectList(
            new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getCourseId, assignment.getCourseId())
        );

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
                .eq(CourseSelection::getCourseId, assignment.getCourseId())
        );

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
                .eq(CourseSelection::getCourseId, assignment.getCourseId())
        );

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

}

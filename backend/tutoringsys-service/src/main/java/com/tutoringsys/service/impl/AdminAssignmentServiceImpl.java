package com.tutoringsys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoringsys.dao.entity.*;
import com.tutoringsys.dao.mapper.*;
import com.tutoringsys.service.AdminAssignmentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AdminAssignmentServiceImpl implements AdminAssignmentService {

    @Resource
    private AssignmentMapper assignmentMapper;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private CourseStudentMapper courseStudentMapper;

    @Resource
    private StudentAnswerMapper studentAnswerMapper;

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private UserMessageMapper userMessageMapper;

    @Override
    public IPage<Map<String, Object>> getAssignmentList(int page, int pageSize, Long courseId, Long teacherId, String status, String keyword) {
        Page<Assignment> queryPage = new Page<>(page, pageSize);
        List<Map<String, Object>> records = new ArrayList<>();

        LambdaQueryWrapper<Assignment> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) {
            wrapper.eq(Assignment::getCourseId, courseId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Assignment::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            String finalKeyword = keyword;
            wrapper.and(q -> 
                q.like(Assignment::getTitle, finalKeyword)
                    .or(sub -> sub.like(Assignment::getDescription, finalKeyword))
            );
        }
        wrapper.orderByDesc(Assignment::getCreateTime);

        IPage<Assignment> assignmentPage = assignmentMapper.selectPage(queryPage, wrapper);
        List<Assignment> assignments = assignmentPage.getRecords();

        for (Assignment assignment : assignments) {
            Map<String, Object> item = new HashMap<>();
            item.put("assignmentId", assignment.getId());
            item.put("assignmentName", assignment.getTitle());
            item.put("totalScore", assignment.getTotalScore());
            item.put("description", assignment.getDescription());
            item.put("deadline", assignment.getDeadline());
            item.put("status", assignment.getStatus());
            item.put("createTime", assignment.getCreateTime());

            Course course = courseMapper.selectById(assignment.getCourseId());
            if (course != null) {
                item.put("courseId", course.getId());
                item.put("courseName", course.getName());

                if (teacherId != null && !teacherId.equals(course.getTeacherId())) {
                    continue;
                }

                User teacher = userMapper.selectById(course.getTeacherId());
                if (teacher != null) {
                    item.put("teacherId", teacher.getId());
                    item.put("teacherName", teacher.getRealName());
                    item.put("teacherAvatar", teacher.getAvatar());
                }
            }

            LambdaQueryWrapper<CourseStudent> csWrapper = new LambdaQueryWrapper<>();
            csWrapper.eq(CourseStudent::getCourseId, assignment.getCourseId());
            int totalStudents = courseStudentMapper.selectCount(csWrapper).intValue();
            item.put("totalStudents", totalStudents);

            LambdaQueryWrapper<StudentAnswer> answerWrapper = new LambdaQueryWrapper<>();
            answerWrapper.eq(StudentAnswer::getAssignmentId, assignment.getId());
            answerWrapper.select(StudentAnswer::getSubmissionId);
            List<StudentAnswer> answers = studentAnswerMapper.selectList(answerWrapper);
            long submittedCount = answers.stream()
                    .map(StudentAnswer::getSubmissionId)
                    .distinct()
                    .count();
            item.put("submittedCount", (int) submittedCount);

            int completionRate = totalStudents > 0 ? (int)(submittedCount * 100 / totalStudents) : 0;
            item.put("completionRate", completionRate);

            if (teacherId != null && item.get("teacherId") != null && !teacherId.equals(item.get("teacherId"))) {
                continue;
            }

            records.add(item);
        }

        Page<Map<String, Object>> resultPage = new Page<>(page, pageSize);
        resultPage.setRecords(records);
        resultPage.setTotal(assignmentPage.getTotal());
        return resultPage;
    }

    @Override
    public Map<String, Object> getAssignmentDetail(Long id) {
        Map<String, Object> result = new HashMap<>();

        Assignment assignment = assignmentMapper.selectById(id);
        if (assignment == null) {
            return result;
        }

        result.put("id", assignment.getId());
        result.put("title", assignment.getTitle());
        result.put("description", assignment.getDescription());
        result.put("totalScore", assignment.getTotalScore());
        result.put("deadline", assignment.getDeadline());
        result.put("status", assignment.getStatus());
        result.put("createTime", assignment.getCreateTime());

        Course course = courseMapper.selectById(assignment.getCourseId());
        if (course != null) {
            result.put("courseId", course.getId());
            result.put("courseName", course.getName());

            User teacher = userMapper.selectById(course.getTeacherId());
            if (teacher != null) {
                result.put("teacherId", teacher.getId());
                result.put("teacherName", teacher.getRealName());
            }
        }

        LambdaQueryWrapper<Question> questionWrapper = new LambdaQueryWrapper<>();
        questionWrapper.eq(Question::getAssignmentId, id);
        questionWrapper.orderByAsc(Question::getSortOrder);
        List<Question> questions = questionMapper.selectList(questionWrapper);
        result.put("questions", questions);

        return result;
    }

    @Override
    public Assignment createAssignment(Map<String, Object> data) {
        Assignment assignment = new Assignment();
        assignment.setCourseId(Long.valueOf(data.get("courseId").toString()));
        assignment.setTitle((String) data.get("title"));
        assignment.setDescription((String) data.get("description"));
        assignment.setTotalScore(data.get("totalScore") != null ? Integer.valueOf(data.get("totalScore").toString()) : 100);
        assignment.setStatus("draft");
        assignment.setCreateTime(new Date());
        assignment.setUpdateTime(new Date());

        assignmentMapper.insert(assignment);
        return assignment;
    }

    @Override
    public Assignment updateAssignment(Long id, Map<String, Object> data) {
        Assignment assignment = assignmentMapper.selectById(id);
        if (assignment == null) {
            return null;
        }

        if (data.get("title") != null) {
            assignment.setTitle((String) data.get("title"));
        }
        if (data.get("description") != null) {
            assignment.setDescription((String) data.get("description"));
        }
        if (data.get("totalScore") != null) {
            assignment.setTotalScore(Integer.valueOf(data.get("totalScore").toString()));
        }

        assignment.setUpdateTime(new Date());
        assignmentMapper.updateById(assignment);
        return assignment;
    }

    @Override
    public boolean deleteAssignment(Long id) {
        assignmentMapper.deleteById(id);
        return true;
    }

    @Override
    public boolean publishAssignment(Long id) {
        Assignment assignment = assignmentMapper.selectById(id);
        if (assignment == null) {
            return false;
        }

        assignment.setStatus("published");
        assignment.setUpdateTime(new Date());
        assignmentMapper.updateById(assignment);
        return true;
    }

    @Override
    public Map<String, Object> getAssignmentSubmissionStatus(Long assignmentId) {
        Map<String, Object> result = new HashMap<>();
        
        Assignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            return result;
        }

        Long courseId = assignment.getCourseId();

        LambdaQueryWrapper<CourseStudent> csWrapper = new LambdaQueryWrapper<>();
        csWrapper.eq(CourseStudent::getCourseId, courseId);
        List<CourseStudent> courseStudents = courseStudentMapper.selectList(csWrapper);

        List<Long> studentIds = courseStudents.stream()
                .map(CourseStudent::getStudentId)
                .collect(java.util.stream.Collectors.toList());

        List<Map<String, Object>> allStudents = new ArrayList<>();
        for (Long studentId : studentIds) {
            User student = userMapper.selectById(studentId);
            if (student != null) {
                Map<String, Object> studentInfo = new HashMap<>();
                studentInfo.put("studentId", student.getId());
                studentInfo.put("studentName", student.getRealName());
                studentInfo.put("username", student.getUsername());
                studentInfo.put("avatar", student.getAvatar());
                studentInfo.put("email", student.getEmail());
                studentInfo.put("phone", student.getPhone());
                studentInfo.put("status", student.getStatus());
                allStudents.add(studentInfo);
            }
        }

        LambdaQueryWrapper<StudentAnswer> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(StudentAnswer::getAssignmentId, assignmentId);
        answerWrapper.select(StudentAnswer::getStudentId, StudentAnswer::getSubmissionId);
        List<StudentAnswer> answers = studentAnswerMapper.selectList(answerWrapper);

        Set<Long> submittedStudentIds = answers.stream()
                .map(StudentAnswer::getStudentId)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        List<Map<String, Object>> submittedStudents = new ArrayList<>();
        List<Map<String, Object>> notSubmittedStudents = new ArrayList<>();

        for (Map<String, Object> studentInfo : allStudents) {
            Long studentId = (Long) studentInfo.get("studentId");
            if (submittedStudentIds.contains(studentId)) {
                submittedStudents.add(studentInfo);
            } else {
                notSubmittedStudents.add(studentInfo);
            }
        }

        result.put("allStudents", allStudents);
        result.put("submittedStudents", submittedStudents);
        result.put("notSubmittedStudents", notSubmittedStudents);
        result.put("totalCount", allStudents.size());
        result.put("submittedCount", submittedStudents.size());
        result.put("notSubmittedCount", notSubmittedStudents.size());
        result.put("assignmentName", assignment.getTitle());

        return result;
    }

    @Override
    public boolean remindStudents(Long assignmentId, Long studentId) {
        Assignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            return false;
        }

        Long targetStudentId = studentId;
        if (targetStudentId == null) {
            LambdaQueryWrapper<StudentAnswer> answerWrapper = new LambdaQueryWrapper<>();
            answerWrapper.eq(StudentAnswer::getAssignmentId, assignmentId);
            answerWrapper.select(StudentAnswer::getStudentId);
            List<StudentAnswer> answers = studentAnswerMapper.selectList(answerWrapper);

            Set<Long> submittedStudentIds = answers.stream()
                    .map(StudentAnswer::getStudentId)
                    .filter(Objects::nonNull)
                    .collect(java.util.stream.Collectors.toSet());

            LambdaQueryWrapper<CourseStudent> csWrapper = new LambdaQueryWrapper<>();
            csWrapper.eq(CourseStudent::getCourseId, assignment.getCourseId());
            List<CourseStudent> courseStudents = courseStudentMapper.selectList(csWrapper);

            for (CourseStudent cs : courseStudents) {
                if (!submittedStudentIds.contains(cs.getStudentId())) {
                    sendReminderMessage(cs.getStudentId(), assignment);
                }
            }
        } else {
            sendReminderMessage(targetStudentId, assignment);
        }

        return true;
    }

    private void sendReminderMessage(Long studentId, Assignment assignment) {
        Course course = courseMapper.selectById(assignment.getCourseId());
        User teacher = null;
        if (course != null && course.getTeacherId() != null) {
            teacher = userMapper.selectById(course.getTeacherId());
        }
        String teacherName = teacher != null ? teacher.getRealName() : "未知";
        String courseName = course != null ? course.getName() : "未知课程";
        
        Message message = new Message();
        message.setTitle("新作业提醒");
        message.setContent(teacherName + "老师教授的《" + courseName + "》课程发布了新作业《" + assignment.getTitle() + "》，快来完成吧~");
        message.setType("assignment");
        message.setRelatedId(assignment.getId());
        message.setRelatedType("assignment");
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);

        UserMessage userMessage = new UserMessage();
        userMessage.setUserId(studentId);
        userMessage.setMessageId(message.getId());
        userMessage.setIsRead(0);
        userMessage.setCreateTime(LocalDateTime.now());
        userMessageMapper.insert(userMessage);
    }
}

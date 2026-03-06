package com.tutoringsys.service.impl;

import com.tutoringsys.common.dto.AssignmentStatsVo;
import com.tutoringsys.common.dto.CourseStatsVo;
import com.tutoringsys.common.exception.BusinessException;
import com.tutoringsys.common.util.RedisUtil;
import com.tutoringsys.dao.entity.Assignment;
import com.tutoringsys.dao.entity.Course;
import com.tutoringsys.dao.entity.CourseStudent;
import com.tutoringsys.dao.entity.StudentAnswer;
import com.tutoringsys.dao.mapper.AssignmentMapper;
import com.tutoringsys.dao.mapper.CourseMapper;
import com.tutoringsys.dao.mapper.CourseStudentMapper;
import com.tutoringsys.dao.mapper.StudentAnswerMapper;
import com.tutoringsys.service.TeacherStatsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class TeacherStatsServiceImpl implements TeacherStatsService {

    @Resource
    private AssignmentMapper assignmentMapper;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private CourseStudentMapper courseStudentMapper;

    @Resource
    private StudentAnswerMapper studentAnswerMapper;

    @Resource
    private RedisUtil redisUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public AssignmentStatsVo getAssignmentStats(Long assignmentId) {
        // 校验作业存在
        Assignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            throw new BusinessException("作业不存在");
        }

        // 校验课程存在且属于当前教师
        Course course = courseMapper.selectById(assignment.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 这里需要校验当前教师是否为课程的teacher_id
        // 暂时简化处理
        AssignmentStatsVo stats = new AssignmentStatsVo();

        // 计算完成率
        long totalStudents = courseStudentMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CourseStudent>()
                        .lambda().eq(CourseStudent::getCourseId, assignment.getCourseId())
        );

        long submittedStudents = studentAnswerMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<StudentAnswer>()
                        .lambda().eq(StudentAnswer::getAssignmentId, assignmentId)
        );

        stats.setCompletionRate(totalStudents > 0 ? (double) submittedStudents / totalStudents * 100 : 0);

        // 计算平均分、最高分、最低分
        List<StudentAnswer> studentAnswers = studentAnswerMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<StudentAnswer>()
                        .lambda().eq(StudentAnswer::getAssignmentId, assignmentId)
        );

        if (!studentAnswers.isEmpty()) {
            int totalScore = 0;
            int highestScore = Integer.MIN_VALUE;
            int lowestScore = Integer.MAX_VALUE;
            int passCount = 0;

            for (StudentAnswer answer : studentAnswers) {
                int score = answer.getScore() != null ? answer.getScore() : 0;
                totalScore += score;
                highestScore = Math.max(highestScore, score);
                lowestScore = Math.min(lowestScore, score);
                if (score >= assignment.getTotalScore() * 0.6) { // 60%及格
                    passCount++;
                }
            }

            stats.setAverageScore((double) totalScore / studentAnswers.size());
            stats.setHighestScore(highestScore);
            stats.setLowestScore(lowestScore);
            stats.setPassRate((double) passCount / studentAnswers.size() * 100);
        }

        // 计算各题型得分率
        List<AssignmentStatsVo.QuestionTypeStats> questionTypeStats = new ArrayList<>();
        // 这里需要根据题目类型分组计算得分率
        // 暂时简化处理
        stats.setQuestionTypeStats(questionTypeStats);

        // 获取高频错题
        String cacheKey = "stats:assignment:" + assignmentId + ":errors";
        List<AssignmentStatsVo.HighFrequencyError> highFrequencyErrors = null;

        try {
            // 尝试从缓存获取
            Object cachedData = redisUtil.get(cacheKey);
            if (cachedData != null) {
                String cachedStr = cachedData.toString();
                highFrequencyErrors = objectMapper.readValue(cachedStr, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, AssignmentStatsVo.HighFrequencyError.class));
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (highFrequencyErrors == null) {
            // 缓存未命中，计算高频错题
            highFrequencyErrors = new ArrayList<>();
            // 这里需要根据错误类型分组统计
            // 暂时简化处理
            // 缓存结果
            try {
                redisUtil.set(cacheKey, objectMapper.writeValueAsString(highFrequencyErrors));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        stats.setHighFrequencyErrors(highFrequencyErrors);

        return stats;
    }

    @Override
    public CourseStatsVo getCourseStats(Long courseId) {
        // 校验课程存在且属于当前教师
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 这里需要校验当前教师是否为课程的teacher_id
        // 暂时简化处理
        CourseStatsVo stats = new CourseStatsVo();
        List<CourseStatsVo.AssignmentStatsItem> assignmentStats = new ArrayList<>();

        // 查询课程下的所有作业
        List<Assignment> assignments = assignmentMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Assignment>()
                        .lambda().eq(Assignment::getCourseId, courseId)
        );

        for (Assignment assignment : assignments) {
            CourseStatsVo.AssignmentStatsItem item = new CourseStatsVo.AssignmentStatsItem();
            item.setAssignmentId(assignment.getId());
            item.setAssignmentTitle(assignment.getTitle());

            // 计算完成率
            long totalStudents = courseStudentMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CourseStudent>()
                            .lambda().eq(CourseStudent::getCourseId, courseId)
            );

            long submittedStudents = studentAnswerMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<StudentAnswer>()
                            .lambda().eq(StudentAnswer::getAssignmentId, assignment.getId())
            );

            item.setCompletionRate(totalStudents > 0 ? (double) submittedStudents / totalStudents * 100 : 0);

            // 计算平均分、最高分、最低分
            List<StudentAnswer> studentAnswers = studentAnswerMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<StudentAnswer>()
                            .lambda().eq(StudentAnswer::getAssignmentId, assignment.getId())
            );

            if (!studentAnswers.isEmpty()) {
                int totalScore = 0;
                int highestScore = Integer.MIN_VALUE;
                int lowestScore = Integer.MAX_VALUE;
                int passCount = 0;

                for (StudentAnswer answer : studentAnswers) {
                    int score = answer.getScore() != null ? answer.getScore() : 0;
                    totalScore += score;
                    highestScore = Math.max(highestScore, score);
                    lowestScore = Math.min(lowestScore, score);
                    if (score >= assignment.getTotalScore() * 0.6) { // 60%及格
                        passCount++;
                    }
                }

                item.setAverageScore((double) totalScore / studentAnswers.size());
                item.setHighestScore(highestScore);
                item.setLowestScore(lowestScore);
                item.setPassRate((double) passCount / studentAnswers.size() * 100);
            }

            assignmentStats.add(item);
        }

        stats.setAssignmentStats(assignmentStats);
        return stats;
    }
}

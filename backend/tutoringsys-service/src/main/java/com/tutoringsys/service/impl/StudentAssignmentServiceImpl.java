package com.tutoringsys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoringsys.common.dto.StudentAssignmentVo;
import com.tutoringsys.common.exception.BusinessException;
import com.tutoringsys.dao.entity.Assignment;
import com.tutoringsys.dao.entity.Course;
import com.tutoringsys.dao.entity.CourseStudent;
import com.tutoringsys.dao.entity.StudentAnswer;
import com.tutoringsys.dao.mapper.AssignmentMapper;
import com.tutoringsys.dao.mapper.CourseMapper;
import com.tutoringsys.dao.mapper.CourseStudentMapper;
import com.tutoringsys.dao.mapper.StudentAnswerMapper;
import com.tutoringsys.common.dto.GradingReportVo;
import com.tutoringsys.common.dto.QuestionReportVo;
import com.tutoringsys.common.dto.SubmissionDto;
import com.tutoringsys.llm.LLMService;
import com.tutoringsys.service.StudentAssignmentService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.UUID;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class StudentAssignmentServiceImpl implements StudentAssignmentService {

    @Resource
    private CourseStudentMapper courseStudentMapper;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private AssignmentMapper assignmentMapper;

    @Resource
    private StudentAnswerMapper studentAnswerMapper;

    @Resource
    private LLMService llmService;

    @Override
    public IPage<StudentAssignmentVo> getAssignmentList(int page, int size, String status) {
        // 获取当前登录学生ID
        Long studentId = getCurrentStudentId();

        // 查询学生已选课
        List<CourseStudent> courseStudents = courseStudentMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CourseStudent>()
                        .lambda().eq(CourseStudent::getStudentId, studentId)
        );

        if (courseStudents.isEmpty()) {
            return new Page<>();
        }

        // 提取课程ID列表
        List<Long> courseIds = courseStudents.stream()
                .map(CourseStudent::getCourseId)
                .toList();

        // 查询课程信息
        List<Course> courses = courseMapper.selectBatchIds(courseIds);

        // 查询作业信息
        List<Assignment> assignments = assignmentMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Assignment>()
                        .lambda().in(Assignment::getCourseId, courseIds)
        );

        // 构建返回数据
        Page<StudentAssignmentVo> resultPage = new Page<>(page, size);
        List<StudentAssignmentVo> voList = assignments.stream()
                .map(assignment -> {
                    StudentAssignmentVo vo = new StudentAssignmentVo();
                    vo.setId(assignment.getId());
                    vo.setTitle(assignment.getTitle());
                    vo.setDeadline(assignment.getEndTime());
                    vo.setCreateTime(assignment.getCreateTime());

                    // 查找课程名称
                    Course course = courses.stream()
                            .filter(c -> c.getId().equals(assignment.getCourseId()))
                            .findFirst()
                            .orElse(null);
                    if (course != null) {
                        vo.setCourseName(course.getName());
                    }

                    // 计算状态
                    Date now = new Date();
                    if (now.after(assignment.getEndTime())) {
                        vo.setStatus("graded");
                    } else {
                        vo.setStatus("pending");
                    }

                    // 这里需要查询学生是否已提交，以及得分情况
                    // 暂时设为0
                    vo.setScore(0);

                    return vo;
                })
                .filter(vo -> status == null || vo.getStatus().equals(status))
                .toList();

        resultPage.setRecords(voList);
        resultPage.setTotal(voList.size());

        return resultPage;
    }

    @Override
    public IPage<Object> getHistoryRecord(int page, int size) {
        // 获取当前登录学生ID
        Long studentId = getCurrentStudentId();

        // 这里需要查询学生的历史提交记录
        // 暂时返回空页面
        return new Page<>();
    }

    private Long getCurrentStudentId() {
        // 从SecurityContext中获取当前用户ID
        // 这里简化处理，实际需要从JWT或Session中获取
        return 1L;
    }

    @Override
    public boolean submitAssignment(SubmissionDto dto) {
        // 获取当前登录学生ID
        Long studentId = getCurrentStudentId();

        // 校验作业存在
        Assignment assignment = assignmentMapper.selectById(dto.getAssignmentId());
        if (assignment == null) {
            throw new BusinessException("作业不存在");
        }

        // 校验作业未过期
        if (new Date().after(assignment.getEndTime())) {
            throw new BusinessException("作业已过期");
        }

        // 生成唯一的submission_id
        String submissionId = UUID.randomUUID().toString();

        // 遍历题目，保存答案
        for (SubmissionDto.AnswerDto answerDto : dto.getAnswers()) {
            StudentAnswer studentAnswer = new StudentAnswer();
            studentAnswer.setSubmissionId(submissionId);
            studentAnswer.setStudentId(studentId);
            studentAnswer.setAssignmentId(dto.getAssignmentId());
            studentAnswer.setQuestionId(answerDto.getQuestionId());
            studentAnswer.setAnswerContent(answerDto.getAnswerContent());
            studentAnswer.setCreateTime(new Date());

            // 这里需要根据题目类型判断是否为客观题
            // 暂时简化处理，假设都是主观题
            studentAnswer.setGraderType("llm");

            // 调用LLM生成反馈
            String feedback = llmService.generateFeedback(answerDto.getAnswerContent(), "");
            studentAnswer.setFeedback(feedback);

            studentAnswerMapper.insert(studentAnswer);
        }

        return true;
    }

    @Override
    public GradingReportVo getGradingReport(String submissionId) {
        // 获取当前登录学生ID
        Long studentId = getCurrentStudentId();

        // 查询该submissionId的所有学生答案
        List<StudentAnswer> studentAnswers = studentAnswerMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<StudentAnswer>()
                        .lambda().eq(StudentAnswer::getSubmissionId, submissionId)
        );

        if (studentAnswers.isEmpty()) {
            throw new BusinessException("提交记录不存在");
        }

        // 确保这些答案属于当前学生
        boolean isOwner = studentAnswers.stream()
                .allMatch(answer -> answer.getStudentId().equals(studentId));
        if (!isOwner) {
            throw new BusinessException("无权限查看该报告");
        }

        // 构建批改报告
        GradingReportVo reportVo = new GradingReportVo();

        // 获取作业标题
        Long assignmentId = studentAnswers.get(0).getAssignmentId();
        Assignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment != null) {
            reportVo.setAssignmentTitle(assignment.getTitle());
        }

        // 构建题目报告列表
        List<QuestionReportVo> questionReports = new ArrayList<>();
        int totalScore = 0;
        int studentScore = 0;

        for (StudentAnswer answer : studentAnswers) {
            QuestionReportVo questionReport = new QuestionReportVo();
            questionReport.setQuestionId(answer.getQuestionId());
            questionReport.setStudentAnswer(answer.getAnswerContent());
            questionReport.setScore(answer.getScore() != null ? answer.getScore() : 0);
            questionReport.setFeedback(answer.getFeedback());

            // 这里需要查询题目内容和标准答案
            // 暂时简化处理
            questionReport.setQuestionContent("题目内容");
            questionReport.setCorrectAnswer("标准答案");
            questionReport.setErrorType("概念错误");

            questionReports.add(questionReport);
            studentScore += questionReport.getScore();
            totalScore += 10; // 假设每题10分
        }

        reportVo.setQuestions(questionReports);
        reportVo.setTotalScore(totalScore);
        reportVo.setStudentScore(studentScore);

        return reportVo;
    }
}

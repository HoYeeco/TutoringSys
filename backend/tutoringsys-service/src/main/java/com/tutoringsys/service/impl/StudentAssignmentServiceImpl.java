package com.tutoringsys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoringsys.common.dto.AssignmentListVo;
import com.tutoringsys.common.dto.GradingReportVo;
import com.tutoringsys.common.dto.SubmissionDto;
import com.tutoringsys.common.exception.BusinessException;
import com.tutoringsys.common.util.HtmlUtils;
import com.tutoringsys.dao.entity.*;
import com.tutoringsys.dao.mapper.*;
import com.tutoringsys.llm.LLMService;
import com.tutoringsys.service.MessageService;
import com.tutoringsys.service.StudentAssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentAssignmentServiceImpl implements StudentAssignmentService {

    @Resource
    private AssignmentMapper assignmentMapper;

    @Resource
    private StudentAnswerMapper studentAnswerMapper;

    @Resource
    private HtmlUtils htmlUtils;

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private LLMService llmService;

    @Resource
    private MessageService messageService;

    @Resource
    private CourseStudentMapper courseStudentMapper;

    @Resource
    private CourseMapper courseMapper;

    @Override
    @Transactional
    public GradingReportVo submitAssignment(Long assignmentId, Long studentId, SubmissionDto dto) {
        Assignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            throw new BusinessException("作业不存在");
        }

        if (assignment.getDeadline() != null && new Date().after(assignment.getDeadline())) {
            throw new BusinessException("作业已逾期，无法提交");
        }

        long count = studentAnswerMapper.selectCount(
                new LambdaQueryWrapper<StudentAnswer>()
                        .eq(StudentAnswer::getAssignmentId, assignmentId)
                        .eq(StudentAnswer::getStudentId, studentId)
        );

        if (count > 0) {
            throw new BusinessException("作业已提交，无法重复提交");
        }

        GradingReportVo report = new GradingReportVo();
        int studentScore = 0;
        int totalScore = 0;

        for (SubmissionDto.AnswerDto answerDto : dto.getAnswers()) {
            Question question = questionMapper.selectById(answerDto.getQuestionId());
            if (question == null) {
                continue;
            }
            totalScore += question.getScore();

            StudentAnswer studentAnswer = new StudentAnswer();
            studentAnswer.setAssignmentId(assignmentId);
            studentAnswer.setStudentId(studentId);
            studentAnswer.setQuestionId(answerDto.getQuestionId());
            String sanitizedAnswer = htmlUtils.sanitizeHtml(answerDto.getAnswerContent());
            studentAnswer.setAnswer(sanitizedAnswer);
            studentAnswer.setAnswerContent(sanitizedAnswer);
            studentAnswer.setSubmitTime(new Date());
            studentAnswer.setStatus(1);
            studentAnswer.setCreateTime(new Date());
            studentAnswer.setUpdateTime(new Date());
            studentAnswer.setVersion(1);

            if ("single".equals(question.getType()) || "multiple".equals(question.getType()) || "judgment".equals(question.getType())) {
                if (answerDto.getAnswerContent().equals(question.getAnswer())) {
                    studentAnswer.setScore(question.getScore());
                    studentAnswer.setFinalScore(question.getScore());
                    studentAnswer.setFeedback("回答正确");
                } else {
                    studentAnswer.setScore(0);
                    studentAnswer.setFinalScore(0);
                    studentAnswer.setFeedback("回答错误，正确答案：" + question.getAnswer());
                }
                studentAnswer.setGraderType("AUTO");
            } else if ("essay".equals(question.getType())) {
                String feedback = llmService.generateFeedback(sanitizedAnswer, question.getContent());
                studentAnswer.setAiFeedback(feedback);
                studentAnswer.setFeedback(feedback);
                studentAnswer.setAiScore(question.getScore() * 80 / 100);
                studentAnswer.setScore(studentAnswer.getAiScore());
                studentAnswer.setGraderType("AI");
                studentAnswer.setStatus(2);
                studentAnswer.setReviewStatus(0);
            }

            studentScore += studentAnswer.getScore();
            studentAnswerMapper.insert(studentAnswer);
        }

        report.setStudentScore(studentScore);
        report.setTotalScore(totalScore > 0 ? totalScore : 100);

        messageService.sendMessage(
                studentId,
                "作业批改完成",
                "您的作业已批改完成，请查看批改报告。",
                "grading",
                assignmentId,
                "assignment"
        );

        Course course = courseMapper.selectById(assignment.getCourseId());
        if (course != null) {
            messageService.sendMessage(
                    course.getTeacherId(),
                    "待复核提醒",
                    "有学生提交了作业需要复核，请及时处理。",
                    "grading",
                    assignmentId,
                    "assignment"
            );
        }

        return report;
    }

    @Override
    public boolean saveDraft(Long assignmentId, Long studentId, SubmissionDto dto) {
        Assignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            throw new BusinessException("作业不存在");
        }
        return true;
    }

    @Override
    public SubmissionDto getDraft(Long assignmentId, Long studentId) {
        Assignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            throw new BusinessException("作业不存在");
        }
        return new SubmissionDto();
    }

    @Override
    public IPage<AssignmentListVo> getAssignmentList(Long studentId, int page, int size, String status) {
        Page<AssignmentListVo> resultPage = new Page<>(page, size);
        List<AssignmentListVo> assignments = new ArrayList<>();

        if (studentId == null) {
            resultPage.setTotal(0);
            resultPage.setRecords(assignments);
            return resultPage;
        }

        LambdaQueryWrapper<CourseStudent> csWrapper = new LambdaQueryWrapper<>();
        csWrapper.eq(CourseStudent::getStudentId, studentId);
        List<CourseStudent> courseStudents = courseStudentMapper.selectList(csWrapper);

        List<Long> courseIds = courseStudents.stream()
                .map(CourseStudent::getCourseId)
                .collect(Collectors.toList());

        if (courseIds.isEmpty()) {
            resultPage.setTotal(0);
            resultPage.setRecords(assignments);
            return resultPage;
        }

        LambdaQueryWrapper<Assignment> assignmentWrapper = new LambdaQueryWrapper<>();
        assignmentWrapper.in(Assignment::getCourseId, courseIds);
        assignmentWrapper.orderByDesc(Assignment::getCreateTime);
        List<Assignment> allAssignments = assignmentMapper.selectList(assignmentWrapper);

        Date now = new Date();
        for (Assignment assignment : allAssignments) {
            AssignmentListVo vo = new AssignmentListVo();
            vo.setId(assignment.getId());
            vo.setTitle(assignment.getTitle());
            vo.setCourseId(assignment.getCourseId());
            vo.setDeadline(assignment.getDeadline());
            vo.setCreateTime(assignment.getCreateTime());
            vo.setTotalScore(assignment.getTotalScore());

            Course course = courseMapper.selectById(assignment.getCourseId());
            vo.setCourseName(course != null ? course.getName() : "未知课程");

            LambdaQueryWrapper<StudentAnswer> answerWrapper = new LambdaQueryWrapper<>();
            answerWrapper.eq(StudentAnswer::getStudentId, studentId);
            answerWrapper.eq(StudentAnswer::getAssignmentId, assignment.getId());
            List<StudentAnswer> answers = studentAnswerMapper.selectList(answerWrapper);

            if (answers.isEmpty()) {
                if (assignment.getDeadline() != null && now.after(assignment.getDeadline())) {
                    vo.setStatus("overdue");
                } else {
                    vo.setStatus("pending");
                }
            } else {
                boolean allGraded = answers.stream()
                        .allMatch(a -> a.getFinalScore() != null || a.getScore() != null);

                if (allGraded) {
                    vo.setStatus("graded");
                    int totalScore = answers.stream()
                            .mapToInt(a -> a.getFinalScore() != null ? a.getFinalScore() : (a.getScore() != null ? a.getScore() : 0))
                            .sum();
                    vo.setScore(totalScore);
                } else {
                    vo.setStatus("grading");
                }
            }

            if (status == null || status.isEmpty() || status.equals(vo.getStatus())) {
                assignments.add(vo);
            }
        }

        int total = assignments.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        List<AssignmentListVo> pageAssignments = start < total ? assignments.subList(start, end) : new ArrayList<>();

        resultPage.setTotal(total);
        resultPage.setRecords(pageAssignments);

        return resultPage;
    }

    @Override
    public Map<String, Object> getAssignmentDetail(Long assignmentId, Long studentId) {
        Map<String, Object> result = new HashMap<>();
        
        Assignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            throw new BusinessException("作业不存在");
        }
        
        Map<String, Object> assignmentMap = new HashMap<>();
        assignmentMap.put("id", assignment.getId());
        assignmentMap.put("title", assignment.getTitle());
        assignmentMap.put("totalScore", assignment.getTotalScore());
        assignmentMap.put("deadline", assignment.getDeadline());
        assignmentMap.put("description", assignment.getDescription());
        
        Course course = courseMapper.selectById(assignment.getCourseId());
        assignmentMap.put("courseName", course != null ? course.getName() : "未知课程");
        
        result.put("assignment", assignmentMap);
        
        LambdaQueryWrapper<Question> questionWrapper = new LambdaQueryWrapper<>();
        questionWrapper.eq(Question::getAssignmentId, assignmentId);
        questionWrapper.orderByAsc(Question::getSortOrder);
        List<Question> questionList = questionMapper.selectList(questionWrapper);
        
        List<Map<String, Object>> questions = new ArrayList<>();
        for (Question question : questionList) {
            Map<String, Object> questionMap = new HashMap<>();
            questionMap.put("id", question.getId());
            questionMap.put("type", question.getType());
            questionMap.put("content", question.getContent());
            questionMap.put("options", question.getOptions());
            questionMap.put("score", question.getScore());
            questionMap.put("maxWords", question.getMaxWords());
            questionMap.put("minWords", question.getMinWords());
            questions.add(questionMap);
        }
        result.put("questions", questions);
        
        return result;
    }
}

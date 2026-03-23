package com.tutoringsys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutoringsys.common.dto.AssignmentCreateDto;
import com.tutoringsys.common.dto.AssignmentUpdateDto;
import com.tutoringsys.common.dto.AssignmentVo;
import com.tutoringsys.common.dto.GradingReportVo;
import com.tutoringsys.common.dto.ReviewDto;
import com.tutoringsys.common.dto.SubmissionListItemVo;
import com.tutoringsys.common.exception.BusinessException;
import com.tutoringsys.common.util.HtmlUtils;
import com.tutoringsys.dao.entity.Assignment;
import com.tutoringsys.dao.entity.Course;
import com.tutoringsys.dao.entity.Question;
import com.tutoringsys.dao.entity.StudentAnswer;
import com.tutoringsys.dao.mapper.AssignmentMapper;
import com.tutoringsys.dao.mapper.CourseMapper;
import com.tutoringsys.dao.mapper.QuestionMapper;
import com.tutoringsys.dao.mapper.StudentAnswerMapper;
import com.tutoringsys.dao.mapper.UserMapper;
import com.tutoringsys.service.MessageService;
import com.tutoringsys.service.TeacherAssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeacherAssignmentServiceImpl implements TeacherAssignmentService {

    @Resource
    private AssignmentMapper assignmentMapper;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private StudentAnswerMapper studentAnswerMapper;

    @Resource
    private HtmlUtils htmlUtils;

    @Override
    @Transactional
    public AssignmentVo createAssignment(AssignmentCreateDto dto) {
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        Assignment assignment = new Assignment();
        assignment.setCourseId(dto.getCourseId());
        assignment.setTitle(dto.getTitle());
        assignment.setDescription(dto.getDescription());
        assignment.setDeadline(dto.getDeadline());
        assignment.setStatus("draft");
        assignment.setCreateTime(new Date());
        assignment.setUpdateTime(new Date());

        assignmentMapper.insert(assignment);

        int totalScore = 0;

        for (AssignmentCreateDto.QuestionDto questionDto : dto.getQuestions()) {
            Question question = new Question();
            question.setAssignmentId(assignment.getId());
            question.setType(questionDto.getType());
            question.setContent(questionDto.getContent());
            question.setOptions(questionDto.getOptions());
            question.setAnswer(questionDto.getAnswer());
            question.setScore(questionDto.getScore());
            question.setAnalysis(questionDto.getAnalysis());
            question.setSortOrder(questionDto.getSortOrder());

            questionMapper.insert(question);
            totalScore += questionDto.getScore();
        }

        assignment.setTotalScore(totalScore);
        assignmentMapper.updateById(assignment);

        return buildAssignmentVo(assignment, course);
    }

    @Override
    @Transactional
    public AssignmentVo updateAssignment(Long id, AssignmentUpdateDto dto) {
        Assignment assignment = assignmentMapper.selectById(id);
        if (assignment == null) {
            throw new BusinessException("作业不存在");
        }

        if (!"draft".equals(assignment.getStatus())) {
            throw new BusinessException("只有草稿状态的作业可以修改");
        }

        Course course = courseMapper.selectById(assignment.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        if (dto.getTitle() != null) {
            assignment.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            assignment.setDescription(dto.getDescription());
        }
        if (dto.getDeadline() != null) {
            assignment.setDeadline(dto.getDeadline());
        }
        assignment.setUpdateTime(new Date());

        questionMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Question>()
                        .lambda().eq(Question::getAssignmentId, id)
        );

        int totalScore = 0;

        for (AssignmentCreateDto.QuestionDto questionDto : dto.getQuestions()) {
            Question question = new Question();
            question.setAssignmentId(assignment.getId());
            question.setType(questionDto.getType());
            question.setContent(questionDto.getContent());
            question.setOptions(questionDto.getOptions());
            question.setAnswer(questionDto.getAnswer());
            question.setScore(questionDto.getScore());
            question.setAnalysis(questionDto.getAnalysis());
            question.setSortOrder(questionDto.getSortOrder());

            questionMapper.insert(question);
            totalScore += questionDto.getScore();
        }

        assignment.setTotalScore(totalScore);
        assignmentMapper.updateById(assignment);

        return buildAssignmentVo(assignment, course);
    }

    @Resource
    private MessageService messageService;

    @Resource
    private UserMapper userMapper;

    @Override
    public AssignmentVo publishAssignment(Long id) {
        Assignment assignment = assignmentMapper.selectById(id);
        if (assignment == null) {
            throw new BusinessException("作业不存在");
        }

        Course course = courseMapper.selectById(assignment.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        assignment.setStatus("published");
        assignment.setUpdateTime(new Date());
        assignmentMapper.updateById(assignment);

        List<Long> studentIds = new ArrayList<>();
        studentIds.add(1L);
        studentIds.add(2L);
        studentIds.add(3L);
        
        com.tutoringsys.dao.entity.User teacher = userMapper.selectById(course.getTeacherId());
        String teacherName = teacher != null ? teacher.getRealName() : "未知";

        messageService.sendMessageToUsers(
                studentIds,
                "新作业提醒",
                teacherName + "老师教授的《" + course.getName() + "》课程发布了新作业《" + assignment.getTitle() + "》，快来完成吧~",
                "assignment",
                assignment.getId(),
                "assignment"
        );

        return buildAssignmentVo(assignment, course);
    }

    private AssignmentVo buildAssignmentVo(Assignment assignment, Course course) {
        AssignmentVo vo = new AssignmentVo();
        vo.setId(assignment.getId());
        vo.setCourseId(assignment.getCourseId());
        vo.setCourseName(course.getName());
        vo.setTitle(assignment.getTitle());
        vo.setDescription(assignment.getDescription());
        vo.setDeadline(assignment.getDeadline());
        vo.setStatus(assignment.getStatus() != null ? Integer.parseInt(assignment.getStatus()) : 0);
        vo.setTotalScore(assignment.getTotalScore());
        vo.setCreateTime(assignment.getCreateTime());
        vo.setUpdateTime(assignment.getUpdateTime());

        List<Question> questions = questionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Question>()
                        .lambda().eq(Question::getAssignmentId, assignment.getId())
                                .orderByAsc(Question::getSortOrder)
        );

        List<AssignmentVo.QuestionDto> questionDtos = questions.stream()
                .map(question -> {
                    AssignmentVo.QuestionDto questionDto = new AssignmentVo.QuestionDto();
                    questionDto.setId(question.getId());
                    questionDto.setType(question.getType());
                    questionDto.setContent(question.getContent());
                    questionDto.setOptions(question.getOptions());
                    questionDto.setAnswer(question.getAnswer());
                    questionDto.setScore(question.getScore());
                    questionDto.setAnalysis(question.getAnalysis());
                    questionDto.setSortOrder(question.getSortOrder());
                    return questionDto;
                })
                .collect(Collectors.toList());

        vo.setQuestions(questionDtos);
        return vo;
    }

    @Override
    public IPage<SubmissionListItemVo> getSubmissionList(Long assignmentId, int page, int size) {
        Assignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            throw new BusinessException("作业不存在");
        }

        Course course = courseMapper.selectById(assignment.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        return new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>();
    }

    @Override
    public GradingReportVo getSubmissionDetail(String submissionId) {
        return new GradingReportVo();
    }

    @Override
    @Transactional
    public boolean reviewSubmission(ReviewDto dto) {
        StudentAnswer studentAnswer = studentAnswerMapper.selectById(dto.getSubmissionId());
        if (studentAnswer == null) {
            throw new BusinessException("提交记录不存在");
        }

        if (studentAnswer.getVersion() != dto.getVersion()) {
            throw new BusinessException("作业已被他人复核，请刷新后查看");
        }

        studentAnswer.setScore(dto.getScore());
        studentAnswer.setFeedback(htmlUtils.sanitizeHtml(dto.getFeedback()));
        studentAnswer.setGraderType("MANUAL");
        studentAnswer.setStatus(2);
        studentAnswer.setUpdateTime(new Date());

        int result = studentAnswerMapper.updateById(studentAnswer);
        if (result == 0) {
            throw new BusinessException("作业已被他人复核，请刷新后查看");
        }

        return true;
    }
}

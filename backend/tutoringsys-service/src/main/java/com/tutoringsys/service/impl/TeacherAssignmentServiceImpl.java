package com.tutoringsys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutoringsys.common.dto.AssignmentCreateDto;
import com.tutoringsys.common.dto.AssignmentUpdateDto;
import com.tutoringsys.common.dto.AssignmentVo;
import com.tutoringsys.common.dto.GradingReportVo;
import com.tutoringsys.common.dto.ReviewDto;
import com.tutoringsys.common.dto.SubmissionListItemVo;
import com.tutoringsys.common.exception.BusinessException;
import com.tutoringsys.dao.entity.Assignment;
import com.tutoringsys.dao.entity.Course;
import com.tutoringsys.dao.entity.Question;
import com.tutoringsys.dao.mapper.AssignmentMapper;
import com.tutoringsys.dao.mapper.CourseMapper;
import com.tutoringsys.dao.mapper.QuestionMapper;
import com.tutoringsys.service.TeacherAssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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

    @Override
    @Transactional
    public AssignmentVo createAssignment(AssignmentCreateDto dto) {
        // 校验课程存在且属于当前教师
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 这里需要校验当前教师是否为课程的teacher_id
        // 暂时简化处理
        // 创建作业
        Assignment assignment = new Assignment();
        assignment.setCourseId(dto.getCourseId());
        assignment.setTitle(dto.getTitle());
        assignment.setDescription(dto.getDescription());
        assignment.setEndTime(dto.getDeadline());
        assignment.setStatus(0); // 0: draft
        assignment.setCreateTime(new Date());
        assignment.setUpdateTime(new Date());

        assignmentMapper.insert(assignment);

        // 计算总分
        int totalScore = 0;

        // 保存题目
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

        // 更新作业总分
        assignment.setTotalScore(totalScore);
        assignmentMapper.updateById(assignment);

        // 构建返回对象
        return buildAssignmentVo(assignment, course);
    }

    @Override
    @Transactional
    public AssignmentVo updateAssignment(Long id, AssignmentUpdateDto dto) {
        // 校验作业存在
        Assignment assignment = assignmentMapper.selectById(id);
        if (assignment == null) {
            throw new BusinessException("作业不存在");
        }

        // 只有草稿状态的作业可以修改
        if (assignment.getStatus() != 0) {
            throw new BusinessException("只有草稿状态的作业可以修改");
        }

        // 校验课程存在且属于当前教师
        Course course = courseMapper.selectById(assignment.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 这里需要校验当前教师是否为课程的teacher_id
        // 暂时简化处理
        // 更新作业信息
        if (dto.getTitle() != null) {
            assignment.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            assignment.setDescription(dto.getDescription());
        }
        if (dto.getDeadline() != null) {
            assignment.setEndTime(dto.getDeadline());
        }
        assignment.setUpdateTime(new Date());

        // 删除原题目
        questionMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Question>()
                        .lambda().eq(Question::getAssignmentId, id)
        );

        // 计算总分
        int totalScore = 0;

        // 保存新题目
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

        // 更新作业总分
        assignment.setTotalScore(totalScore);
        assignmentMapper.updateById(assignment);

        // 构建返回对象
        return buildAssignmentVo(assignment, course);
    }

    @Override
    public AssignmentVo publishAssignment(Long id) {
        // 校验作业存在
        Assignment assignment = assignmentMapper.selectById(id);
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
        // 发布作业
        assignment.setStatus(1); // 1: published
        assignment.setUpdateTime(new Date());
        assignmentMapper.updateById(assignment);

        // 构建返回对象
        return buildAssignmentVo(assignment, course);
    }

    private AssignmentVo buildAssignmentVo(Assignment assignment, Course course) {
        AssignmentVo vo = new AssignmentVo();
        vo.setId(assignment.getId());
        vo.setCourseId(assignment.getCourseId());
        vo.setCourseName(course.getName());
        vo.setTitle(assignment.getTitle());
        vo.setDescription(assignment.getDescription());
        vo.setDeadline(assignment.getEndTime());
        vo.setStatus(assignment.getStatus());
        vo.setTotalScore(assignment.getTotalScore());
        vo.setCreateTime(assignment.getCreateTime());
        vo.setUpdateTime(assignment.getUpdateTime());

        // 查询题目
        List<Question> questions = questionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Question>()
                        .lambda().eq(Question::getAssignmentId, assignment.getId())
                                .orderByAsc(Question::getSortOrder)
        );

        // 转换题目
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
        // 这里需要查询学生提交列表
        // 暂时返回空页面
        return new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>();
    }

    @Override
    public GradingReportVo getSubmissionDetail(String submissionId) {
        // 这里需要查询提交详情
        // 暂时返回空对象
        return new GradingReportVo();
    }

    @Override
    public boolean reviewSubmission(ReviewDto dto) {
        // 这里需要实现人工复核逻辑
        // 暂时返回true
        return true;
    }
}

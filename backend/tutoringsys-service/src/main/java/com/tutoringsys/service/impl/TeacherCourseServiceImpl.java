package com.tutoringsys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoringsys.common.dto.CourseStudentVo;
import com.tutoringsys.common.exception.BusinessException;
import com.tutoringsys.dao.entity.Course;
import com.tutoringsys.dao.entity.CourseStudent;
import com.tutoringsys.dao.entity.User;
import com.tutoringsys.dao.mapper.CourseMapper;
import com.tutoringsys.dao.mapper.CourseStudentMapper;
import com.tutoringsys.dao.mapper.UserMapper;
import com.tutoringsys.service.TeacherCourseService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TeacherCourseServiceImpl implements TeacherCourseService {

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private CourseStudentMapper courseStudentMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public IPage<CourseStudentVo> getCourseStudents(Long courseId, int page, int size) {
        // 校验课程存在且属于当前教师
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 这里需要校验当前教师是否为课程的teacher_id
        // 暂时简化处理
        // 查询课程的学生
        List<CourseStudent> courseStudents = courseStudentMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CourseStudent>()
                        .lambda().eq(CourseStudent::getCourseId, courseId)
        );

        if (courseStudents.isEmpty()) {
            return new Page<>();
        }

        // 提取学生ID列表
        List<Long> studentIds = courseStudents.stream()
                .map(CourseStudent::getStudentId)
                .toList();

        // 查询学生信息
        List<User> students = userMapper.selectBatchIds(studentIds);

        // 构建返回数据
        Page<CourseStudentVo> resultPage = new Page<>(page, size);
        List<CourseStudentVo> voList = students.stream()
                .map(student -> {
                    CourseStudentVo vo = new CourseStudentVo();
                    vo.setId(student.getId());
                    vo.setUsername(student.getUsername());
                    vo.setRealName(student.getRealName());
                    vo.setEmail(student.getEmail());
                    vo.setPhone(student.getPhone());
                    return vo;
                })
                .toList();

        resultPage.setRecords(voList);
        resultPage.setTotal(voList.size());

        return resultPage;
    }

    @Override
    public boolean addStudents(Long courseId, List<Long> studentIds) {
        // 校验课程存在且属于当前教师
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 这里需要校验当前教师是否为课程的teacher_id
        // 暂时简化处理
        // 批量添加学生
        for (Long studentId : studentIds) {
            // 校验学生存在且角色为STUDENT
            User student = userMapper.selectById(studentId);
            if (student == null) {
                throw new BusinessException("学生不存在");
            }
            if (!"STUDENT".equals(student.getRole())) {
                throw new BusinessException("指定用户不是学生角色");
            }

            // 检查学生是否已经在课程中
            CourseStudent existing = courseStudentMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CourseStudent>()
                            .lambda().eq(CourseStudent::getCourseId, courseId)
                                    .eq(CourseStudent::getStudentId, studentId)
            );

            if (existing == null) {
                CourseStudent courseStudent = new CourseStudent();
                courseStudent.setCourseId(courseId);
                courseStudent.setStudentId(studentId);
                courseStudentMapper.insert(courseStudent);
            }
        }

        return true;
    }

    @Override
    public boolean removeStudent(Long courseId, Long studentId) {
        // 校验课程存在且属于当前教师
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 这里需要校验当前教师是否为课程的teacher_id
        // 暂时简化处理
        // 检查学生是否在课程中
        CourseStudent existing = courseStudentMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CourseStudent>()
                        .lambda().eq(CourseStudent::getCourseId, courseId)
                                .eq(CourseStudent::getStudentId, studentId)
        );

        if (existing == null) {
            throw new BusinessException("学生不在该课程中");
        }

        // 这里可以添加检查学生是否有作业提交记录的逻辑
        // 暂时简化处�?
        // 从课程中移除学生
        courseStudentMapper.deleteById(existing.getId());

        return true;
    }
}

package com.tutoringsys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoringsys.common.dto.AdminCourseCreateDto;
import com.tutoringsys.common.dto.AdminCourseUpdateDto;
import com.tutoringsys.common.dto.AdminCourseVo;
import com.tutoringsys.common.exception.BusinessException;
import com.tutoringsys.dao.entity.Course;
import com.tutoringsys.dao.entity.User;
import com.tutoringsys.dao.mapper.CourseMapper;
import com.tutoringsys.service.CourseService;
import com.tutoringsys.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class CourseServiceImpl implements CourseService {

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private UserService userService;

    @Override
    public IPage<AdminCourseVo> pageCourses(int page, int size, Map<String, Object> params) {
        Page<Course> coursePage = new Page<>(page, size);
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();

        if (params.containsKey("name")) {
            queryWrapper.like(Course::getName, params.get("name"));
        }
        if (params.containsKey("teacherId")) {
            queryWrapper.eq(Course::getTeacherId, params.get("teacherId"));
        }

        IPage<Course> courses = courseMapper.selectPage(coursePage, queryWrapper);
        return courses.convert(course -> {
            AdminCourseVo vo = new AdminCourseVo();
            vo.setId(course.getId());
            vo.setName(course.getName());
            vo.setDescription(course.getDescription());
            vo.setTeacherId(course.getTeacherId());
            // 关联查询教师姓名
            User teacher = userService.getUserById(course.getTeacherId());
            if (teacher != null) {
                vo.setTeacherName(teacher.getRealName());
            }
            vo.setCreateTime(course.getCreateTime());
            vo.setUpdateTime(course.getUpdateTime());
            return vo;
        });
    }

    @Override
    public Course createCourse(AdminCourseCreateDto dto) {
        // 校验教师ID存在且角色为TEACHER
        User teacher = userService.getUserById(dto.getTeacherId());
        if (teacher == null) {
            throw new BusinessException("教师不存在");
        }
        if (!"TEACHER".equals(teacher.getRole())) {
            throw new BusinessException("指定用户不是教师角色");
        }

        Course course = new Course();
        course.setName(dto.getName());
        course.setDescription(dto.getDescription());
        course.setTeacherId(dto.getTeacherId());

        courseMapper.insert(course);
        return course;
    }

    @Override
    public Course updateCourse(Long id, AdminCourseUpdateDto dto) {
        Course course = getCourseById(id);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 校验教师ID存在且角色为TEACHER
        if (dto.getTeacherId() != null) {
            User teacher = userService.getUserById(dto.getTeacherId());
            if (teacher == null) {
                throw new BusinessException("教师不存在");
            }
            if (!"TEACHER".equals(teacher.getRole())) {
                throw new BusinessException("指定用户不是教师角色");
            }
            course.setTeacherId(dto.getTeacherId());
        }

        if (dto.getName() != null) {
            course.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            course.setDescription(dto.getDescription());
        }

        courseMapper.updateById(course);
        return course;
    }

    @Override
    public void deleteCourse(Long id) {
        Course course = getCourseById(id);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 检查是否有作业关联
        // 这里可以添加检查逻辑，如是否有作业等

        courseMapper.deleteById(id);
    }

    @Override
    public Course getCourseById(Long id) {
        return courseMapper.selectById(id);
    }
}

package com.tutoring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.AdminCourseVO;
import com.tutoring.dto.AdminUpdateCourseRequest;
import com.tutoring.entity.*;
import com.tutoring.mapper.*;
import com.tutoring.service.AdminCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCourseServiceImpl implements AdminCourseService {

    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final CourseSelectionMapper courseSelectionMapper;
    private final AssignmentMapper assignmentMapper;

    @Override
    public Page<AdminCourseVO> listCourses(Integer page, Integer size, Long teacherId, String keyword) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();

        if (teacherId != null) {
            wrapper.eq(Course::getTeacherId, teacherId);
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Course::getName, keyword);
        }

        wrapper.orderByDesc(Course::getCreateTime);

        Page<Course> coursePage = courseMapper.selectPage(new Page<>(page, size), wrapper);

        List<Long> courseIds = coursePage.getRecords().stream()
            .map(Course::getId)
            .collect(Collectors.toList());

        List<Long> teacherIds = coursePage.getRecords().stream()
            .map(Course::getTeacherId)
            .distinct()
            .collect(Collectors.toList());

        Map<Long, User> teacherMap = teacherIds.isEmpty() ? Map.of() :
            userMapper.selectList(
                new LambdaQueryWrapper<User>()
                    .in(User::getId, teacherIds)
            ).stream().collect(Collectors.toMap(User::getId, Function.identity()));

        Map<Long, Integer> studentCountMap = courseIds.isEmpty() ? Map.of() :
            courseSelectionMapper.selectList(
                new LambdaQueryWrapper<CourseSelection>()
                    .in(CourseSelection::getCourseId, courseIds)
            ).stream().collect(Collectors.groupingBy(
                CourseSelection::getCourseId,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));

        Map<Long, Integer> assignmentCountMap = courseIds.isEmpty() ? Map.of() :
            assignmentMapper.selectList(
                new LambdaQueryWrapper<Assignment>()
                    .in(Assignment::getCourseId, courseIds)
            ).stream().collect(Collectors.groupingBy(
                Assignment::getCourseId,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));

        List<AdminCourseVO> voList = coursePage.getRecords().stream()
            .map(course -> {
                User teacher = teacherMap.get(course.getTeacherId());
                return AdminCourseVO.builder()
                    .id(course.getId())
                    .name(course.getName())
                    .description(course.getDescription())
                    .teacherId(course.getTeacherId())
                    .teacherName(teacher != null ? teacher.getRealName() : "未知教师")
                    .studentCount(studentCountMap.getOrDefault(course.getId(), 0))
                    .assignmentCount(assignmentCountMap.getOrDefault(course.getId(), 0))
                    .createTime(course.getCreateTime())
                    .updateTime(course.getUpdateTime())
                    .build();
            })
            .collect(Collectors.toList());

        Page<AdminCourseVO> voPage = new Page<>(coursePage.getCurrent(), coursePage.getSize(), coursePage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public AdminCourseVO getCourseById(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            return null;
        }

        User teacher = userMapper.selectById(course.getTeacherId());

        Integer studentCount = Math.toIntExact(courseSelectionMapper.selectCount(
            new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getCourseId, id)
        ));

        Integer assignmentCount = Math.toIntExact(assignmentMapper.selectCount(
            new LambdaQueryWrapper<Assignment>()
                .eq(Assignment::getCourseId, id)
        ));

        return AdminCourseVO.builder()
            .id(course.getId())
            .name(course.getName())
            .description(course.getDescription())
            .teacherId(course.getTeacherId())
            .teacherName(teacher != null ? teacher.getRealName() : "未知教师")
            .studentCount(studentCount)
            .assignmentCount(assignmentCount)
            .createTime(course.getCreateTime())
            .updateTime(course.getUpdateTime())
            .build();
    }

    @Override
    public AdminCourseVO updateCourse(Long id, AdminUpdateCourseRequest request) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }

        User teacher = userMapper.selectById(request.getTeacherId());
        if (teacher == null) {
            throw new RuntimeException("教师不存在");
        }

        if (!"TEACHER".equals(teacher.getRole())) {
            throw new RuntimeException("指定用户不是教师");
        }

        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setTeacherId(request.getTeacherId());

        courseMapper.updateById(course);

        return getCourseById(id);
    }

    @Override
    public void deleteCourse(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }

        Integer studentCount = Math.toIntExact(courseSelectionMapper.selectCount(
            new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getCourseId, id)
        ));

        Integer assignmentCount = Math.toIntExact(assignmentMapper.selectCount(
            new LambdaQueryWrapper<Assignment>()
                .eq(Assignment::getCourseId, id)
        ));

        if (studentCount > 0 || assignmentCount > 0) {
            throw new RuntimeException("该课程下有 " + studentCount + " 名学生和 " + assignmentCount + " 个作业，请先移除关联数据");
        }

        courseMapper.deleteById(id);
    }
}

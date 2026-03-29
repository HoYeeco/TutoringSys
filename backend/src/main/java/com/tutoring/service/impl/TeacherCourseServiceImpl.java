package com.tutoring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.*;
import com.tutoring.entity.*;
import com.tutoring.exception.BusinessException;
import com.tutoring.mapper.*;
import com.tutoring.service.MessageService;
import com.tutoring.service.TeacherCourseService;
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
public class TeacherCourseServiceImpl implements TeacherCourseService {

    private final CourseMapper courseMapper;
    private final CourseSelectionMapper courseSelectionMapper;
    private final AssignmentMapper assignmentMapper;
    private final UserMapper userMapper;
    private final MessageService messageService;
    private final UserMessageMapper userMessageMapper;

    @Override
    @Cacheable(value = "teacherCourses", key = "'list:' + #teacherId + ':page:' + #page + ':size:' + #size + ':keyword:' + (#keyword ?: '')", unless = "#result == null || #result.records.isEmpty()")
    public Page<TeacherCourseVO> getCourseList(Long teacherId, Integer page, Integer size, String keyword) {
        log.info("从数据库查询教师课程列表: teacherId={}", teacherId);
        
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<Course>()
            .eq(Course::getTeacherId, teacherId);

        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(w -> w
                .like(Course::getName, keyword)
                .or()
                .like(Course::getDescription, keyword)
            );
        }

        queryWrapper.orderByDesc(Course::getCreateTime);

        Page<Course> coursePage = courseMapper.selectPage(new Page<>(page, size), queryWrapper);

        List<Long> courseIds = coursePage.getRecords().stream()
            .map(Course::getId)
            .collect(Collectors.toList());

        Map<Long, Integer> studentCountMap = new HashMap<>();
        Map<Long, Integer> assignmentCountMap = new HashMap<>();

        if (!courseIds.isEmpty()) {
            List<CourseSelection> selections = courseSelectionMapper.selectList(
                new LambdaQueryWrapper<CourseSelection>()
                    .in(CourseSelection::getCourseId, courseIds)
            );

            studentCountMap = selections.stream()
                .collect(Collectors.groupingBy(CourseSelection::getCourseId, Collectors.collectingAndThen(
                    Collectors.mapping(CourseSelection::getStudentId, Collectors.toSet()),
                    Set::size
                )));

            List<Assignment> assignments = assignmentMapper.selectList(
                new LambdaQueryWrapper<Assignment>()
                    .in(Assignment::getCourseId, courseIds)
                    .eq(Assignment::getStatus, "published")
            );

            assignmentCountMap = assignments.stream()
                .collect(Collectors.groupingBy(Assignment::getCourseId, Collectors.counting()))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().intValue()));
        }

        Map<Long, Integer> finalStudentCountMap = studentCountMap;
        Map<Long, Integer> finalAssignmentCountMap = assignmentCountMap;

        List<TeacherCourseVO> voList = coursePage.getRecords().stream()
            .map(course -> TeacherCourseVO.builder()
                .id(course.getId())
                .name(course.getName())
                .description(course.getDescription())
                .teacherId(course.getTeacherId())
                .studentCount(finalStudentCountMap.getOrDefault(course.getId(), 0))
                .assignmentCount(finalAssignmentCountMap.getOrDefault(course.getId(), 0))
                .createTime(course.getCreateTime())
                .updateTime(course.getUpdateTime())
                .build())
            .collect(Collectors.toList());

        Page<TeacherCourseVO> resultPage = new Page<>(page, size, coursePage.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }

    @Override
    @Cacheable(value = "teacherCourses", key = "'detail:' + #teacherId + ':' + #courseId")
    public TeacherCourseVO getCourseDetail(Long teacherId, Long courseId) {
        log.info("从数据库查询课程详情: teacherId={}, courseId={}", teacherId, courseId);
        
        Course course = courseMapper.selectById(courseId);
        if (course == null || !course.getTeacherId().equals(teacherId)) {
            return null;
        }

        List<CourseSelection> selections = courseSelectionMapper.selectList(
            new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getCourseId, courseId)
        );

        List<Long> studentIds = selections.stream()
            .map(CourseSelection::getStudentId)
            .collect(Collectors.toList());

        List<TeacherCourseVO.StudentInfo> students = new ArrayList<>();
        if (!studentIds.isEmpty()) {
            List<User> studentUsers = userMapper.selectBatchIds(studentIds);
            students = studentUsers.stream()
                .map(u -> TeacherCourseVO.StudentInfo.builder()
                    .id(u.getId())
                    .username(u.getUsername())
                    .realName(u.getRealName())
                    .build())
                .collect(Collectors.toList());
        }

        Integer assignmentCount = assignmentMapper.selectCount(
            new LambdaQueryWrapper<Assignment>()
                .eq(Assignment::getCourseId, courseId)
                .eq(Assignment::getStatus, "published")
        ).intValue();

        return TeacherCourseVO.builder()
            .id(course.getId())
            .name(course.getName())
            .description(course.getDescription())
            .teacherId(course.getTeacherId())
            .studentCount(studentIds.size())
            .assignmentCount(assignmentCount)
            .createTime(course.getCreateTime())
            .updateTime(course.getUpdateTime())
            .students(students)
            .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = "teacherCourses", key = "'list:' + #teacherId + ':*'")
    public Long createCourse(Long teacherId, CreateCourseRequest request) {
        log.info("创建课程，清除教师课程缓存: teacherId={}", teacherId);
        
        Course course = new Course();
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setTeacherId(teacherId);
        courseMapper.insert(course);

        if (request.getStudentIds() != null && !request.getStudentIds().isEmpty()) {
            for (Long studentId : request.getStudentIds()) {
                CourseSelection selection = new CourseSelection();
                selection.setCourseId(course.getId());
                selection.setStudentId(studentId);
                courseSelectionMapper.insert(selection);
            }

            User teacher = userMapper.selectById(teacherId);
            String teacherName = teacher != null ? teacher.getRealName() : "未知";
            notifyStudents(request.getStudentIds(), 
                "课程添加通知", 
                "你已加入" + teacherName + "老师的" + course.getName() + "课程", 
                teacherId);
        }

        return course.getId();
    }

    @Override
    @Transactional
    @CacheEvict(value = "teacherCourses", allEntries = true)
    public void updateCourse(Long teacherId, UpdateCourseRequest request) {
        log.info("更新课程，清除教师课程缓存: courseId={}", request.getId());
        
        Course course = courseMapper.selectById(request.getId());
        if (course == null || !course.getTeacherId().equals(teacherId)) {
            throw new BusinessException("课程不存在或无权访问");
        }

        course.setName(request.getName());
        course.setDescription(request.getDescription());
        courseMapper.updateById(course);

        List<CourseSelection> existingSelections = courseSelectionMapper.selectList(
            new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getCourseId, request.getId())
        );

        Set<Long> existingStudentIds = existingSelections.stream()
            .map(CourseSelection::getStudentId)
            .collect(Collectors.toSet());

        Set<Long> newStudentIds = request.getStudentIds() != null 
            ? new HashSet<>(request.getStudentIds()) 
            : new HashSet<>();

        List<Long> toAdd = newStudentIds.stream()
            .filter(id -> !existingStudentIds.contains(id))
            .collect(Collectors.toList());

        List<Long> toRemove = existingStudentIds.stream()
            .filter(id -> !newStudentIds.contains(id))
            .collect(Collectors.toList());

        if (!toAdd.isEmpty()) {
            for (Long studentId : toAdd) {
                CourseSelection selection = new CourseSelection();
                selection.setCourseId(request.getId());
                selection.setStudentId(studentId);
                courseSelectionMapper.insert(selection);
            }

            User teacher = userMapper.selectById(teacherId);
            String teacherName = teacher != null ? teacher.getRealName() : "未知";
            notifyStudents(toAdd, 
                "课程添加通知", 
                "你已加入" + teacherName + "老师的" + course.getName() + "课程", 
                teacherId);
        }

        if (!toRemove.isEmpty()) {
            courseSelectionMapper.delete(
                new LambdaQueryWrapper<CourseSelection>()
                    .eq(CourseSelection::getCourseId, request.getId())
                    .in(CourseSelection::getStudentId, toRemove)
            );

            User teacher = userMapper.selectById(teacherId);
            String teacherName = teacher != null ? teacher.getRealName() : "未知";
            notifyStudents(toRemove, 
                "课程移除通知", 
                "你已被移出" + teacherName + "老师的" + course.getName() + "课程", 
                teacherId);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "teacherCourses", allEntries = true)
    public void deleteCourse(Long teacherId, Long courseId) {
        log.info("删除课程，清除教师课程缓存: courseId={}", courseId);
        
        Course course = courseMapper.selectById(courseId);
        if (course == null || !course.getTeacherId().equals(teacherId)) {
            throw new BusinessException("课程不存在或无权访问");
        }

        courseMapper.deleteById(courseId);

        courseSelectionMapper.delete(
            new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getCourseId, courseId)
        );
    }

    private void notifyStudents(List<Long> studentIds, String title, String content, Long teacherId) {
        if (studentIds == null || studentIds.isEmpty()) {
            return;
        }

        Message message = new Message();
        message.setTitle(title);
        message.setContent(content);
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

package com.tutoringsys.api.controller.teacher;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutoringsys.api.dto.CourseStudentVo;
import com.tutoringsys.api.dto.StudentIdsDto;
import com.tutoringsys.common.response.R;
import com.tutoringsys.service.TeacherCourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/teacher/courses")
@PreAuthorize("hasRole('TEACHER')")
@Tag(name = "教师课程管理", description = "教师课程相关接口")
public class TeacherCourseController {

    @Resource
    private TeacherCourseService teacherCourseService;

    @GetMapping("/{courseId}/students")
    @Operation(summary = "获取课程学生列表", description = "获取指定课程的学生列表，支持分页")
    public R<IPage<CourseStudentVo>> getCourseStudents(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小，默认10") @RequestParam(defaultValue = "10") int size) {
        IPage<CourseStudentVo> studentPage = teacherCourseService.getCourseStudents(courseId, page, size);
        return R.ok(studentPage);
    }

    @PostMapping("/{courseId}/students")
    @Operation(summary = "添加学生到课程", description = "将学生添加到指定课程")
    public R<Boolean> addStudents(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "学生ID列表") @RequestBody StudentIdsDto dto) {
        boolean result = teacherCourseService.addStudents(courseId, dto.getStudentIds());
        return R.ok(result);
    }

    @DeleteMapping("/{courseId}/students/{studentId}")
    @Operation(summary = "从课程移除学生", description = "将学生从指定课程中移除")
    public R<Boolean> removeStudent(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "学生ID") @PathVariable Long studentId) {
        boolean result = teacherCourseService.removeStudent(courseId, studentId);
        return R.ok(result);
    }
}
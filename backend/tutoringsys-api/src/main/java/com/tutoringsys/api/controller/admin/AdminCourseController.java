package com.tutoringsys.api.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutoringsys.api.dto.AdminCourseCreateDto;
import com.tutoringsys.api.dto.AdminCourseUpdateDto;
import com.tutoringsys.api.dto.AdminCourseVo;
import com.tutoringsys.common.response.R;
import com.tutoringsys.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/courses")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "管理员课程管理", description = "管理员课程相关接口")
public class AdminCourseController {

    @Resource
    private CourseService courseService;

    @GetMapping
    @Operation(summary = "获取课程列表", description = "获取课程列表，支持分页和筛选")
    public R<IPage<AdminCourseVo>> pageCourses(
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小，默认10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "课程名称") @RequestParam(required = false) String name,
            @Parameter(description = "教师ID") @RequestParam(required = false) Long teacherId) {
        Map<String, Object> params = new HashMap<>();
        if (name != null) params.put("name", name);
        if (teacherId != null) params.put("teacherId", teacherId);
        IPage<AdminCourseVo> coursePage = courseService.pageCourses(page, size, params);
        return R.ok(coursePage);
    }

    @PostMapping
    @Operation(summary = "创建课程", description = "创建新课程")
    public R<AdminCourseVo> createCourse(
            @Parameter(description = "课程创建数据") @RequestBody AdminCourseCreateDto dto) {
        var course = courseService.createCourse(dto);
        AdminCourseVo vo = new AdminCourseVo();
        vo.setId(course.getId());
        vo.setName(course.getName());
        vo.setDescription(course.getDescription());
        vo.setTeacherId(course.getTeacherId());
        vo.setCreateTime(course.getCreateTime());
        vo.setUpdateTime(course.getUpdateTime());
        return R.ok(vo);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新课程", description = "更新课程信息")
    public R<AdminCourseVo> updateCourse(
            @Parameter(description = "课程ID") @PathVariable Long id, 
            @Parameter(description = "课程更新数据") @RequestBody AdminCourseUpdateDto dto) {
        var course = courseService.updateCourse(id, dto);
        AdminCourseVo vo = new AdminCourseVo();
        vo.setId(course.getId());
        vo.setName(course.getName());
        vo.setDescription(course.getDescription());
        vo.setTeacherId(course.getTeacherId());
        vo.setCreateTime(course.getCreateTime());
        vo.setUpdateTime(course.getUpdateTime());
        return R.ok(vo);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除课程", description = "删除指定课程")
    public R<Void> deleteCourse(
            @Parameter(description = "课程ID") @PathVariable Long id) {
        courseService.deleteCourse(id);
        return R.ok();
    }
}
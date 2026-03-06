package com.tutoringsys.api.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutoringsys.api.dto.QuestionCreateDto;
import com.tutoringsys.api.dto.QuestionUpdateDto;
import com.tutoringsys.common.response.R;
import com.tutoringsys.dao.entity.Question;
import com.tutoringsys.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/questions")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "管理员题目管理", description = "管理员题目相关接口")
public class AdminQuestionController {

    @Resource
    private QuestionService questionService;

    @GetMapping
    @Operation(summary = "获取题目列表", description = "获取题目列表，支持分页和筛选")
    public R<IPage<Question>> pageQuestions(
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小，默认10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "题目类型") @RequestParam(required = false) String type,
            @Parameter(description = "题目内容") @RequestParam(required = false) String content) {
        Map<String, Object> params = new HashMap<>();
        if (type != null) params.put("type", type);
        if (content != null) params.put("content", content);
        IPage<Question> questionPage = questionService.pageQuestions(page, size, params);
        return R.ok(questionPage);
    }

    @PostMapping
    @Operation(summary = "创建题目", description = "创建新题目")
    public R<Question> createQuestion(
            @Parameter(description = "题目创建数据") @RequestBody QuestionCreateDto dto) {
        Question question = questionService.createQuestion(dto);
        return R.ok(question);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新题目", description = "更新题目信息")
    public R<Question> updateQuestion(
            @Parameter(description = "题目ID") @PathVariable Long id, 
            @Parameter(description = "题目更新数据") @RequestBody QuestionUpdateDto dto) {
        Question question = questionService.updateQuestion(id, dto);
        return R.ok(question);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除题目", description = "删除指定题目")
    public R<Void> deleteQuestion(
            @Parameter(description = "题目ID") @PathVariable Long id) {
        questionService.deleteQuestion(id);
        return R.ok();
    }

    @PostMapping("/import")
    @Operation(summary = "导入题目", description = "从Excel文件导入题目")
    public R<Map<String, Object>> importQuestions(
            @Parameter(description = "Excel文件") @RequestParam("file") MultipartFile file) {
        Map<String, Object> result = questionService.importQuestions(file);
        return R.ok(result);
    }

    @GetMapping("/export")
    @Operation(summary = "导出题目", description = "导出题目到Excel文件")
    public void exportQuestions(
            HttpServletResponse response,
            @Parameter(description = "题目类型") @RequestParam(required = false) String type,
            @Parameter(description = "题目内容") @RequestParam(required = false) String content) {
        Map<String, Object> params = new HashMap<>();
        if (type != null) params.put("type", type);
        if (content != null) params.put("content", content);
        questionService.exportQuestions(response, params);
    }
}
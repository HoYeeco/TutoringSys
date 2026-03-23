package com.tutoringsys.api.controller.student;

import com.tutoringsys.common.response.R;
import com.tutoringsys.service.DraftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student/draft")
@PreAuthorize("hasRole('STUDENT')")
@Tag(name = "学生草稿管理", description = "学生草稿相关接口")
public class DraftController {

    @Resource
    private DraftService draftService;

    @PostMapping("/{assignmentId}")
    @Operation(summary = "保存草稿", description = "保存作业草稿")
    public R<Boolean> saveDraft(
            @PathVariable Long assignmentId,
            @RequestBody Map<String, Object> draftData) {
        Long studentId = 1L;
        boolean result = draftService.saveDraft(studentId, assignmentId, draftData);
        return R.ok(result);
    }

    @GetMapping("/{assignmentId}")
    @Operation(summary = "获取草稿", description = "获取作业草稿")
    public R<Map<String, Object>> getDraft(@PathVariable Long assignmentId) {
        Long studentId = 1L;
        Map<String, Object> draft = draftService.getDraft(studentId, assignmentId);
        return R.ok(draft);
    }

    @DeleteMapping("/{assignmentId}")
    @Operation(summary = "删除草稿", description = "删除作业草稿")
    public R<Boolean> deleteDraft(@PathVariable Long assignmentId) {
        Long studentId = 1L;
        boolean result = draftService.deleteDraft(studentId, assignmentId);
        return R.ok(result);
    }

    @GetMapping("/list")
    @Operation(summary = "获取草稿列表", description = "获取学生所有草稿列表")
    public R<List<Map<String, Object>>> getDraftList() {
        Long studentId = 1L;
        List<Map<String, Object>> drafts = draftService.getDraftList(studentId);
        return R.ok(drafts);
    }
}

package com.tutoringsys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoringsys.api.dto.*;
import com.tutoringsys.service.TeacherAssignmentService;
import org.springframework.stereotype.Service;

@Service
public class TeacherAssignmentServiceImpl implements TeacherAssignmentService {

    @Override
    public AssignmentVo createAssignment(AssignmentCreateDto dto) {
        // 简化实现
        return new AssignmentVo();
    }

    @Override
    public AssignmentVo updateAssignment(Long id, AssignmentUpdateDto dto) {
        // 简化实现
        return new AssignmentVo();
    }

    @Override
    public AssignmentVo publishAssignment(Long id) {
        // 简化实现
        return new AssignmentVo();
    }

    @Override
    public IPage<SubmissionListItemVo> getSubmissionList(Long assignmentId, int page, int size) {
        // 简化实现
        return new Page<>();
    }

    @Override
    public GradingReportVo getSubmissionDetail(String submissionId) {
        // 简化实现
        return new GradingReportVo();
    }

    @Override
    public boolean reviewSubmission(ReviewDto dto) {
        // 简化实现
        return true;
    }
}

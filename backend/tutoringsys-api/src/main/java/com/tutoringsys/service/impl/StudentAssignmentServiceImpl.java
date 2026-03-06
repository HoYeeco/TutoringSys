package com.tutoringsys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoringsys.api.dto.*;
import com.tutoringsys.service.StudentAssignmentService;
import org.springframework.stereotype.Service;

@Service
public class StudentAssignmentServiceImpl implements StudentAssignmentService {

    @Override
    public IPage<StudentAssignmentVo> getAssignmentList(int page, int size, String status) {
        // 简化实现
        return new Page<>();
    }

    @Override
    public StudentAssignmentVo getAssignmentDetail(Long assignmentId) {
        // 简化实现
        return new StudentAssignmentVo();
    }

    @Override
    public GradingReportVo submitAssignment(SubmissionDto dto) {
        // 简化实现
        return new GradingReportVo();
    }

    @Override
    public IPage<SubmissionListItemVo> getSubmissionHistory(int page, int size) {
        // 简化实现
        return new Page<>();
    }

    @Override
    public GradingReportVo getSubmissionReport(String submissionId) {
        // 简化实现
        return new GradingReportVo();
    }
}

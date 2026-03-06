package com.tutoringsys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutoringsys.common.dto.StudentAssignmentVo;

import com.tutoringsys.common.dto.SubmissionDto;

import com.tutoringsys.common.dto.GradingReportVo;

public interface StudentAssignmentService {
    IPage<StudentAssignmentVo> getAssignmentList(int page, int size, String status);
    IPage<Object> getHistoryRecord(int page, int size);
    boolean submitAssignment(SubmissionDto dto);
    GradingReportVo getGradingReport(String submissionId);
}

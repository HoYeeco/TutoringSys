package com.tutoringsys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutoringsys.api.dto.*;

public interface StudentAssignmentService {
    IPage<StudentAssignmentVo> getAssignmentList(int page, int size, String status);
    StudentAssignmentVo getAssignmentDetail(Long assignmentId);
    GradingReportVo submitAssignment(SubmissionDto dto);
    IPage<SubmissionListItemVo> getSubmissionHistory(int page, int size);
    GradingReportVo getSubmissionReport(String submissionId);
}

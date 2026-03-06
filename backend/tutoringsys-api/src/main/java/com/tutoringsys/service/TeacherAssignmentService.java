package com.tutoringsys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutoringsys.api.dto.*;

public interface TeacherAssignmentService {
    AssignmentVo createAssignment(AssignmentCreateDto dto);
    AssignmentVo updateAssignment(Long id, AssignmentUpdateDto dto);
    AssignmentVo publishAssignment(Long id);
    IPage<SubmissionListItemVo> getSubmissionList(Long assignmentId, int page, int size);
    GradingReportVo getSubmissionDetail(String submissionId);
    boolean reviewSubmission(ReviewDto dto);
}

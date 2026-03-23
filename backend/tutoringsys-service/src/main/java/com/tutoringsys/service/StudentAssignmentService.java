package com.tutoringsys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutoringsys.common.dto.AssignmentListVo;
import com.tutoringsys.common.dto.GradingReportVo;
import com.tutoringsys.common.dto.SubmissionDto;

import java.util.Map;

public interface StudentAssignmentService {
    GradingReportVo submitAssignment(Long assignmentId, Long studentId, SubmissionDto dto);
    boolean saveDraft(Long assignmentId, Long studentId, SubmissionDto dto);
    SubmissionDto getDraft(Long assignmentId, Long studentId);
    IPage<AssignmentListVo> getAssignmentList(Long studentId, int page, int size, String status);
    Map<String, Object> getAssignmentDetail(Long assignmentId, Long studentId);
}

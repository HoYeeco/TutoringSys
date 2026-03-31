package com.tutoring.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.AdminAssignmentVO;
import com.tutoring.dto.CreateAssignmentRequest;
import com.tutoring.dto.SubmissionDetailVO;
import com.tutoring.dto.SubmissionRecordVO;
import com.tutoring.dto.UpdateAssignmentRequest;

public interface AdminAssignmentService {

    Page<AdminAssignmentVO> listAssignments(Integer page, Integer size, Long courseId, 
            String status, String keyword);

    AdminAssignmentVO getAssignmentDetail(Long id);

    void deleteAssignment(Long id);

    Long createAssignment(CreateAssignmentRequest request);
    
    void updateAssignment(Long id, UpdateAssignmentRequest request);

    void deleteSubmission(Long submissionId);

    Page<SubmissionRecordVO> getAssignmentSubmissions(Long assignmentId, Integer page, Integer size);

    SubmissionDetailVO getSubmissionDetail(Long submissionId);
}

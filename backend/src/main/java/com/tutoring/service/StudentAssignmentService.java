package com.tutoring.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.AssignmentDetailVO;
import com.tutoring.dto.SaveDraftRequest;
import com.tutoring.dto.StudentAssignmentListVO;
import com.tutoring.dto.SubmitRequest;
import com.tutoring.dto.SubmitResponse;

public interface StudentAssignmentService {

    Page<StudentAssignmentListVO> getAssignmentList(Long studentId, Integer page, Integer size,
            String status, Long courseId, String keyword, String sortBy, String sortOrder);

    AssignmentDetailVO getAssignmentDetail(Long studentId, Long assignmentId);

    void saveDraft(Long studentId, Long assignmentId, SaveDraftRequest request);

    SubmitResponse submitAssignment(Long studentId, Long assignmentId, SubmitRequest request);

}

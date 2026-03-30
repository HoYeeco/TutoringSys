package com.tutoring.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.AdminAssignmentVO;
import com.tutoring.dto.CreateAssignmentRequest;

public interface AdminAssignmentService {

    Page<AdminAssignmentVO> listAssignments(Integer page, Integer size, Long courseId, 
            String status, String keyword);

    AdminAssignmentVO getAssignmentDetail(Long id);

    void deleteAssignment(Long id);

    Long createAssignment(CreateAssignmentRequest request);
}

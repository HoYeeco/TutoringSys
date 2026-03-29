package com.tutoring.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.CreateAssignmentRequest;
import com.tutoring.dto.TeacherAssignmentVO;
import com.tutoring.dto.UpdateAssignmentRequest;

public interface TeacherAssignmentService {

    Page<TeacherAssignmentVO> getAssignmentList(Long teacherId, Integer page, Integer size,
            String status, Long courseId, String keyword);

    TeacherAssignmentVO getAssignmentDetail(Long teacherId, Long assignmentId);

    Long createAssignment(Long teacherId, CreateAssignmentRequest request);

    void updateAssignment(Long teacherId, UpdateAssignmentRequest request);

    void publishAssignment(Long teacherId, Long assignmentId);

    void deleteAssignment(Long teacherId, Long assignmentId);

}

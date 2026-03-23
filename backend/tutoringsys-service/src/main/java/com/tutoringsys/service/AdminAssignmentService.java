package com.tutoringsys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutoringsys.dao.entity.Assignment;

import java.util.List;
import java.util.Map;

public interface AdminAssignmentService {
    IPage<Map<String, Object>> getAssignmentList(int page, int pageSize, Long courseId, Long teacherId, String status, String keyword);
    Map<String, Object> getAssignmentDetail(Long id);
    Assignment createAssignment(Map<String, Object> data);
    Assignment updateAssignment(Long id, Map<String, Object> data);
    boolean deleteAssignment(Long id);
    boolean publishAssignment(Long id);
    Map<String, Object> getAssignmentSubmissionStatus(Long assignmentId);
    boolean remindStudents(Long assignmentId, Long studentId);
}

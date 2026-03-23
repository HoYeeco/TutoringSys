package com.tutoringsys.service;

import com.tutoringsys.common.dto.GradingDetailVo;
import com.tutoringsys.common.dto.GradingListVo;

import java.util.List;
import java.util.Map;

public interface StudentGradingService {
    
    Map<String, Object> getGradingList(Long studentId, int page, int size, Long courseId, String sortBy, String sortOrder);
    
    GradingDetailVo getGradingDetail(Long submissionId, Long studentId);
    
    boolean addToErrorBook(Long studentId, Long questionId);
    
    boolean batchAddToErrorBook(Long studentId, List<Long> questionIds);
}

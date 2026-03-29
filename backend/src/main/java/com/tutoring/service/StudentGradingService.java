package com.tutoring.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.GradingDetailVO;
import com.tutoring.dto.GradingListVO;

public interface StudentGradingService {

    Page<GradingListVO> getGradingList(Long studentId, Integer page, Integer size,
            Long courseId, String keyword, String sortBy, String sortOrder);

    GradingDetailVO getGradingDetail(Long studentId, Long submissionId);

}

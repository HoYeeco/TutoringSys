package com.tutoring.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.SubmissionDetailVO;
import com.tutoring.dto.SubmissionRecordVO;

import java.time.LocalDateTime;

public interface AdminSubmissionService {

    Page<SubmissionRecordVO> listSubmissions(Integer page, Integer size, Long courseId, 
            Integer reviewStatus, LocalDateTime startTime, LocalDateTime endTime);

    SubmissionDetailVO getSubmissionDetail(Long id);
}

package com.tutoring.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.ErrorBookListVO;
import com.tutoring.dto.ErrorBookStatVO;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface StudentErrorBookService {

    List<ErrorBookStatVO> getErrorBookStats(Long studentId);

    Page<ErrorBookListVO> getErrorBookList(Long studentId, Integer page, Integer size,
            Long courseId, String type, String keyword);

    void addToErrorBook(Long studentId, Long questionId, Long assignmentId);

    Map<String, Object> batchAddToErrorBook(Long studentId, List<Map<String, Long>> questions);

    void removeFromErrorBook(Long studentId, Long errorBookId);

    void exportErrorBook(Long studentId, Long courseId, String type, String format,
            HttpServletResponse response);

    Set<Long> checkInErrorBook(Long studentId, Long assignmentId, List<Long> questionIds);

}

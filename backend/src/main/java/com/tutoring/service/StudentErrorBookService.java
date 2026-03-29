package com.tutoring.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.ErrorBookListVO;
import com.tutoring.dto.ErrorBookStatVO;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface StudentErrorBookService {

    List<ErrorBookStatVO> getErrorBookStats(Long studentId);

    Page<ErrorBookListVO> getErrorBookList(Long studentId, Integer page, Integer size,
            Long courseId, String type, String keyword);

    void removeFromErrorBook(Long studentId, Long errorBookId);

    void exportErrorBook(Long studentId, Long courseId, String type, String format,
            HttpServletResponse response);

}

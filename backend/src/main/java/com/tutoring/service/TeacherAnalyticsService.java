package com.tutoring.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.ErrorAnalysisVO;
import com.tutoring.dto.FrequentErrorVO;
import com.tutoring.dto.OverviewStatsVO;
import com.tutoring.dto.StudentTrendVO;
import jakarta.servlet.http.HttpServletResponse;

public interface TeacherAnalyticsService {

    OverviewStatsVO getOverviewStats(Long teacherId);

    ErrorAnalysisVO getErrorAnalysis(Long teacherId, Long courseId);

    Page<FrequentErrorVO> getFrequentErrors(Long teacherId, Integer page, Integer size, Long courseId);

    void exportFrequentErrors(Long teacherId, Long courseId, HttpServletResponse response);

    StudentTrendVO getStudentTrend(Long teacherId, Long studentId);

}

package com.tutoring.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.ErrorAnalysisVO;
import com.tutoring.dto.FrequentErrorVO;
import com.tutoring.dto.MasteryHeatmapVO;
import com.tutoring.dto.OverviewStatsVO;
import com.tutoring.dto.StudentTrendVO;

public interface TeacherAnalyticsService {

    OverviewStatsVO getOverviewStats(Long teacherId);

    ErrorAnalysisVO getErrorAnalysis(Long teacherId, Long courseId);

    Page<FrequentErrorVO> getFrequentErrors(Long teacherId, Integer page, Integer size, Long courseId);

    byte[] exportFrequentErrors(Long teacherId, Long courseId);

    StudentTrendVO getStudentTrend(Long teacherId, Long studentId);

    MasteryHeatmapVO getMasteryHeatmap(Long teacherId, Long courseId);

}

package com.tutoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentTrendVO {

    private Long studentId;
    private String studentName;
    private List<ScoreTrend> trends;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreTrend {
        private Long assignmentId;
        private String assignmentTitle;
        private Integer studentScore;
        private Integer totalScore;
        private Double studentPercentage;
        private Double classAverage;
        private String submitTime;
    }
}

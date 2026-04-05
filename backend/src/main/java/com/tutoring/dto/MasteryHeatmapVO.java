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
public class MasteryHeatmapVO {

    private List<StudentInfo> students;
    private List<QuestionInfo> questions;
    private List<ScoreData> scores;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentInfo {
        private Long id;
        private String name;
        private String account;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionInfo {
        private Long id;
        private String content;
        private Integer maxScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreData {
        private Long studentId;
        private Long questionId;
        private Integer score;
        private Integer maxScore;
        private Double scoreRate;
    }
}

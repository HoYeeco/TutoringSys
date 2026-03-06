package com.tutoringsys.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class AssignmentStatsVo {
    private double completionRate;
    private double averageScore;
    private int highestScore;
    private int lowestScore;
    private double passRate;
    private List<QuestionTypeStats> questionTypeStats;
    private List<HighFrequencyError> highFrequencyErrors;

    @Data
    public static class QuestionTypeStats {
        private String type;
        private double correctRate;
    }

    @Data
    public static class HighFrequencyError {
        private Long questionId;
        private String questionContent;
        private String errorType;
        private int errorCount;
    }
}

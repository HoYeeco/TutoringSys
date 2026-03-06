package com.tutoringsys.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class CourseStatsVo {
    private List<AssignmentStatsItem> assignmentStats;

    @Data
    public static class AssignmentStatsItem {
        private Long assignmentId;
        private String assignmentTitle;
        private double completionRate;
        private double averageScore;
        private int highestScore;
        private int lowestScore;
        private double passRate;
    }
}

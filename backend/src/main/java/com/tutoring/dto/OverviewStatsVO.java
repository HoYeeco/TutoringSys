package com.tutoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverviewStatsVO {

    private Integer totalCourses;
    private Integer totalAssignments;
    private Integer totalStudents;
    private Integer totalSubmissions;
    private Double completionRate;
    private Double averageScore;
    private Double excellentRate;
    private List<ScoreDistribution> scoreDistribution;
    private List<CourseStats> courseStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreDistribution {
        private String range;
        private Integer count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseStats {
        private Long courseId;
        private String courseName;
        private Integer assignmentCount;
        private Integer studentCount;
        private Double averageScore;
        private Double completionRate;
    }
}

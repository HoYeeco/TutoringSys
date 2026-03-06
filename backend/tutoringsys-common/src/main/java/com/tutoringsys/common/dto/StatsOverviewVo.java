package com.tutoringsys.common.dto;

import lombok.Data;

@Data
public class StatsOverviewVo {
    private long totalUsers;
    private long totalCourses;
    private long totalAssignments;
    private long todaySubmissions;
    private long llmCallsToday;
}

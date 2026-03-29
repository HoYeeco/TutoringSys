package com.tutoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDashboardVO {

    private Long selectedCourses;
    private Long pendingAssignments;
    private Long gradedSubmissions;
    private BigDecimal averageScore;

}

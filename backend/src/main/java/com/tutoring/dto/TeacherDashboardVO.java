package com.tutoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDashboardVO {

    private Long courseCount;
    private Long studentCount;
    private Long ongoingAssignmentCount;
    private Long pendingReviewCount;
    private Long overdueAssignmentCount;
}

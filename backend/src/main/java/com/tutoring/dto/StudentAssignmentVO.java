package com.tutoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAssignmentVO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private Integer totalScore;
    private String status;
    private Integer score;
    private LocalDateTime gradeTime;
}

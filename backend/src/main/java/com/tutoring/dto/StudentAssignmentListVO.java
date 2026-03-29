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
public class StudentAssignmentListVO {

    private Long id;
    private String title;
    private String description;
    private Long courseId;
    private String courseName;
    private Long teacherId;
    private String teacherName;
    private LocalDateTime deadline;
    private Integer totalScore;
    private String status;
    private Integer finalScore;
    private LocalDateTime submitTime;

}

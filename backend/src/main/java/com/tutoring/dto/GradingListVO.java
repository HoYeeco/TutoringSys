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
public class GradingListVO {

    private Long submissionId;
    private Long assignmentId;
    private String assignmentTitle;
    private Long courseId;
    private String courseName;
    private Long teacherId;
    private String teacherName;
    private Integer totalScore;
    private Integer finalTotalScore;
    private LocalDateTime submitTime;
    private LocalDateTime reviewTime;
}

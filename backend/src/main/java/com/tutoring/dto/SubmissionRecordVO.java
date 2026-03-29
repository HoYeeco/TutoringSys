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
public class SubmissionRecordVO {

    private Long id;
    private String submissionId;
    private Long studentId;
    private String studentName;
    private String studentUsername;
    private Long assignmentId;
    private String assignmentTitle;
    private Long courseId;
    private String courseName;
    private Long teacherId;
    private String teacherName;
    private Integer status;
    private Integer totalScore;
    private Integer aiTotalScore;
    private Integer finalTotalScore;
    private Integer reviewStatus;
    private LocalDateTime submitTime;
    private LocalDateTime reviewTime;
}

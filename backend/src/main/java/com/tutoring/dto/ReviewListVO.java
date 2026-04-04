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
public class ReviewListVO {

    private Long answerId;
    private Long studentId;
    private String studentName;
    private Long assignmentId;
    private String assignmentTitle;
    private Long courseId;
    private String courseName;
    private Long questionId;
    private String questionType;
    private String questionContent;
    private Integer questionScore;
    private Integer aiScore;
    private Integer finalScore;
    private Integer reviewStatus;
    private String graderType;
    private LocalDateTime submitTime;
    private LocalDateTime aiGradeTime;
    private Integer submissionStatus;
    private Integer submissionReviewStatus;
}

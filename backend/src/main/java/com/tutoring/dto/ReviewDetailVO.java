package com.tutoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDetailVO {

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
    private List<String> questionOptions;
    private Integer maxScore;
    private String studentAnswer;
    private String correctAnswer;
    private Integer aiScore;
    private String aiFeedback;
    private Integer finalScore;
    private String teacherFeedback;
    private String graderType;
    private Integer reviewStatus;
    private LocalDateTime submitTime;
    private LocalDateTime aiGradeTime;
}

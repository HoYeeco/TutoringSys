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
public class GradingDetailVO {

    private Long submissionId;
    private Long assignmentId;
    private String assignmentTitle;
    private String assignmentDescription;
    private Long courseId;
    private String courseName;
    private Long teacherId;
    private String teacherName;
    private Integer totalScore;
    private Integer finalTotalScore;
    private Integer aiTotalScore;
    private LocalDateTime submitTime;
    private LocalDateTime reviewTime;
    private String teacherFeedback;
    private List<AnswerDetailVO> answers;
}

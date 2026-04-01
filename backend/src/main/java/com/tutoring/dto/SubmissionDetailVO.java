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
public class SubmissionDetailVO {

    private Long id;
    private String submissionId;
    private Long studentId;
    private String studentName;
    private String studentUsername;
    private String studentAvatar;
    private Long assignmentId;
    private String assignmentTitle;
    private Long courseId;
    private String courseName;
    private Integer totalScore;
    private Integer aiTotalScore;
    private Integer finalTotalScore;
    private Integer reviewStatus;
    private LocalDateTime submitTime;
    private LocalDateTime reviewTime;
    private List<AnswerDetail> answers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerDetail {
        private Long id;
        private Long questionId;
        private String questionType;
        private String questionContent;
        private Integer questionScore;
        private String answer;
        private String answerContent;
        private Integer score;
        private Integer aiScore;
        private Integer finalScore;
        private String aiFeedback;
        private String teacherFeedback;
        private String graderType;
        private Integer reviewStatus;
        private LocalDateTime reviewTime;
    }
}

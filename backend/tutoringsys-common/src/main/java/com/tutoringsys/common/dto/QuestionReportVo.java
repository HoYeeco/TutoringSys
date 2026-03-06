package com.tutoringsys.common.dto;

import lombok.Data;

@Data
public class QuestionReportVo {
    private Long questionId;
    private String questionContent;
    private String studentAnswer;
    private String correctAnswer;
    private int score;
    private String errorType;
    private String feedback;
}

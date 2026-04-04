package com.tutoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerDetailVO {

    private Long questionId;
    private String type;
    private String content;
    private List<String> options;
    private Integer maxScore;
    private String studentAnswer;
    private String correctAnswer;
    private String referenceAnswer;
    private Boolean isCorrect;
    private Integer score;
    private Integer finalScore;
    private String analysis;
    private String aiFeedback;
    private String teacherFeedback;
}

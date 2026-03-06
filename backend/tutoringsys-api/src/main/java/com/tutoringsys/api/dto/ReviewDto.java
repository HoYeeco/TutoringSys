package com.tutoringsys.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReviewDto {
    private String submissionId;
    private List<QuestionReview> questions;

    @Data
    public static class QuestionReview {
        private Long questionId;
        private Integer score;
        private String feedback;
    }
}
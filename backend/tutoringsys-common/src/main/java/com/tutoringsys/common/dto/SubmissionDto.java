package com.tutoringsys.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class SubmissionDto {
    private Long assignmentId;
    private List<AnswerDto> answers;

    @Data
    public static class AnswerDto {
        private Long questionId;
        private String answerContent;
    }
}

package com.tutoringsys.common.dto;

import lombok.Data;

@Data
public class QuestionCreateDto {
    private String type;
    private String content;
    private String options;
    private String answer;
    private Integer score;
    private String analysis;
    private Integer sortOrder;
}

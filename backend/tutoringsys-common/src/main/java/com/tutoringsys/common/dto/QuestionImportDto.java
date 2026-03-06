package com.tutoringsys.common.dto;

import lombok.Data;

@Data
public class QuestionImportDto {
    private String type;
    private String content;
    private String options;
    private String answer;
    private Integer score;
    private String analysis;
}

package com.tutoringsys.api.dto;

import lombok.Data;

@Data
public class QuestionExportVo {
    private String type;
    private String content;
    private String options;
    private String answer;
    private Integer score;
    private String analysis;
}
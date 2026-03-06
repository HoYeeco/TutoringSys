package com.tutoringsys.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class GradingReportVo {
    private String assignmentTitle;
    private int totalScore;
    private int studentScore;
    private List<QuestionReportVo> questions;
}
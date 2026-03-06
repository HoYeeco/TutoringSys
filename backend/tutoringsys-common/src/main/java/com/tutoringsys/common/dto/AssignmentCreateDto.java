package com.tutoringsys.common.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AssignmentCreateDto {
    private Long courseId;
    private String title;
    private String description;
    private Date deadline;
    private List<QuestionDto> questions;

    @Data
    public static class QuestionDto {
        private Long questionId; // 来自题库的题目ID，为空则新建
        private String type;
        private String content;
        private String options;
        private String answer;
        private Integer score;
        private String analysis;
        private Integer sortOrder;
    }
}

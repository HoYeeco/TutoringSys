package com.tutoringsys.common.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AssignmentVo {
    private Long id;
    private Long courseId;
    private String courseName;
    private String title;
    private String description;
    private Date deadline;
    private Integer status;
    private Integer totalScore;
    private Date createTime;
    private Date updateTime;
    private List<QuestionDto> questions;

    @Data
    public static class QuestionDto {
        private Long id;
        private String type;
        private String content;
        private String options;
        private String answer;
        private Integer score;
        private String analysis;
        private Integer sortOrder;
    }
}

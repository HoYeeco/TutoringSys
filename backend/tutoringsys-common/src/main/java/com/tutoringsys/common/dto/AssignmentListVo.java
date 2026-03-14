package com.tutoringsys.common.dto;

import lombok.Data;

import java.util.Date;

@Data
public class AssignmentListVo {
    private Long id;
    private String title;
    private String courseName;
    private Long courseId;
    private Date deadline;
    private Date createTime;
    private String status;
    private Integer score;
    private Integer totalScore;
}
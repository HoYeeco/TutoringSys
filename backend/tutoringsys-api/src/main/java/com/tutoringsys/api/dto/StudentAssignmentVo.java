package com.tutoringsys.api.dto;

import lombok.Data;

import java.util.Date;

@Data
public class StudentAssignmentVo {
    private Long id;
    private String title;
    private String courseName;
    private Date deadline;
    private String status;
    private Integer score;
    private Date createTime;
}
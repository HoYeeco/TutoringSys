package com.tutoringsys.api.dto;

import lombok.Data;

import java.util.Date;

@Data
public class SubmissionListItemVo {
    private String submissionId;
    private Long studentId;
    private String studentName;
    private Date submitTime;
    private Integer totalScore;
    private Integer gradedScore;
}
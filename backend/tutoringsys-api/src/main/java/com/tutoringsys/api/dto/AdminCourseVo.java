package com.tutoringsys.api.dto;

import lombok.Data;

import java.util.Date;

@Data
public class AdminCourseVo {
    private Long id;
    private String name;
    private String description;
    private Long teacherId;
    private String teacherName;
    private Date createTime;
    private Date updateTime;
}
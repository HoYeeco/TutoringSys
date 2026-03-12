package com.tutoringsys.common.dto;

import lombok.Data;

import java.util.Date;

@Data
public class TeacherCourseVo {
    private Long id;
    private String name;
    private String description;
    private Long teacherId;
    private int studentCount;
    private Date createTime;
    private Date updateTime;
}
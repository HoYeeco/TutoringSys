package com.tutoringsys.api.dto;

import lombok.Data;

@Data
public class AdminCourseUpdateDto {
    private String name;
    private String description;
    private Long teacherId;
}
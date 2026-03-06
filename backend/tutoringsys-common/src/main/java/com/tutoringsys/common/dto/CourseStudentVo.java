package com.tutoringsys.common.dto;

import lombok.Data;

@Data
public class CourseStudentVo {
    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
}

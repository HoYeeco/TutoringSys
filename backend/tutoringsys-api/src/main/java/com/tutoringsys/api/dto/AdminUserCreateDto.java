package com.tutoringsys.api.dto;

import lombok.Data;

@Data
public class AdminUserCreateDto {
    private String username;
    private String password;
    private String realName;
    private String role;
    private String email;
    private String phone;
    private Integer status;
}
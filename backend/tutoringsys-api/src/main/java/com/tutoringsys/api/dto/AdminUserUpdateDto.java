package com.tutoringsys.api.dto;

import lombok.Data;

@Data
public class AdminUserUpdateDto {
    private String realName;
    private String email;
    private String phone;
    private String role;
    private Integer status;
    private String password; // 可选字段，若提供则更新密码
}
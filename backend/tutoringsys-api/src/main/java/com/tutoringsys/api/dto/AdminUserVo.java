package com.tutoringsys.api.dto;

import lombok.Data;

import java.util.Date;

@Data
public class AdminUserVo {
    private Long id;
    private String username;
    private String realName;
    private String role;
    private String avatar;
    private String email;
    private String phone;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
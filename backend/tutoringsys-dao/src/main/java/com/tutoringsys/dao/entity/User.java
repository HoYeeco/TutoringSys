package com.tutoringsys.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("user")
public class User {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String role;
    private String avatar;
    private String email;
    private String phone;
    private Integer status;
    private Date createTime;
    private Date updateTime;
    private Integer deleted;
}
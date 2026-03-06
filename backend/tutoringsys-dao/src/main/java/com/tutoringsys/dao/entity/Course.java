package com.tutoringsys.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("course")
public class Course {
    private Long id;
    private String name;
    private String description;
    private Long teacherId;
    private Date createTime;
    private Date updateTime;
}
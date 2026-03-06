package com.tutoringsys.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("assignment")
public class Assignment {
    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private Date startTime;
    private Date endTime;
    private Integer status;
    private Integer totalScore;
    private Date createTime;
    private Date updateTime;
}
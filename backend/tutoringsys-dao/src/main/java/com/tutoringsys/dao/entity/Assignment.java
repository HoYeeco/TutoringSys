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
    private Date deadline;
    private Integer totalScore;
    private String status;
    private Date createTime;
    private Date updateTime;
}

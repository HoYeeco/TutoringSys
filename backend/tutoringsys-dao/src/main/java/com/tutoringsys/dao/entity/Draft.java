package com.tutoringsys.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("draft")
public class Draft {
    private Long id;
    private Long studentId;
    private Long assignmentId;
    private Long questionId;
    private String answer;
    private String answerContent;
    private Date createTime;
    private Date updateTime;
}

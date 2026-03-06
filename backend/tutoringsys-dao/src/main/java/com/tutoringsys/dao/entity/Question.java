package com.tutoringsys.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("question")
public class Question {
    private Long id;
    private Long assignmentId;
    private String type;
    private String content;
    private String options;
    private String answer;
    private Integer score;
    private String analysis;
    private Integer sortOrder;
}
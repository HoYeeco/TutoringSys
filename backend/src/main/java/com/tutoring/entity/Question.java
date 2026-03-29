package com.tutoring.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "question", autoResultMap = true)
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long assignmentId;
    private String type;
    private String content;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> options;
    private String answer;
    private String referenceAnswer;
    private Integer score;
    private Integer minWords;
    private Integer maxWords;
    private String analysis;
    private Integer sortOrder;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

package com.tutoring.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("assignment")
public class Assignment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Long teacherId;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private Integer totalScore;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

package com.tutoring.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String type;
    private Long relatedId;
    private String relatedType;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

package com.tutoring.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_message")
public class UserMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long messageId;
    private Integer isRead;
    private LocalDateTime readTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

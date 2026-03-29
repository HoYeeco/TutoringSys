package com.tutoring.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("error_book")
public class ErrorBook {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long studentAnswerId;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

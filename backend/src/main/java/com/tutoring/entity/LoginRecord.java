package com.tutoring.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("login_record")
public class LoginRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDateTime loginTime;
    private String ipAddress;
    private String userAgent;
    private Integer success;
}

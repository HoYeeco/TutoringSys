package com.tutoring.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("audit_log")
public class AuditLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String operationType;
    private String operationContent;
    private String operator;
    private String operatorRole;
    private String ipAddress;
    private Integer success;
    private String errorMessage;
    private LocalDateTime operationTime;
}

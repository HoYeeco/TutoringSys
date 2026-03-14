package com.tutoringsys.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("audit_log")
public class AuditLog {
    private Long id;
    private String operationType;
    private String operationContent;
    private String operator;
    private String operatorRole;
    private String ipAddress;
    private boolean success;
    private String errorMessage;
    private Date operationTime;
}
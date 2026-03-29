package com.tutoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogVO {

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

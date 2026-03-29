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
public class LoginRecordResponse {

    private Long id;
    private LocalDateTime loginTime;
    private String ipAddress;
    private String userAgent;
    private Integer success;

}

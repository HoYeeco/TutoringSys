package com.tutoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLMMonitorVO {

    private Long totalCalls;
    private Long successCalls;
    private Long failedCalls;
    private Double successRate;
    private Long totalPromptTokens;
    private Long totalCompletionTokens;
    private Long totalTokens;
    private List<FailedCallRecord> recentFailures;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedCallRecord {
        private Long id;
        private String requestId;
        private Long userId;
        private String username;
        private String modelName;
        private String errorMessage;
        private String requestTime;
    }
}

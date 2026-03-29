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
public class ErrorAnalysisVO {

    private List<ErrorItem> errorByType;
    private List<ErrorItem> errorByCourse;
    private List<ErrorItem> errorByAssignment;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorItem {
        private String name;
        private Integer value;
        private Long id;
    }
}

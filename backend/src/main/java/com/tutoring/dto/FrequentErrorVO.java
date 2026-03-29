package com.tutoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrequentErrorVO {

    private Long questionId;
    private String questionType;
    private String questionContent;
    private Long assignmentId;
    private String assignmentTitle;
    private Long courseId;
    private String courseName;
    private Integer errorCount;
    private Integer totalCount;
    private Double errorRate;
}

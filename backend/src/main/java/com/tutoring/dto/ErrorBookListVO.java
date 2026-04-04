package com.tutoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorBookListVO {

    private Long id;
    private Long questionId;
    private String type;
    private String content;
    private List<String> options;
    private String studentAnswer;
    private String correctAnswer;
    private String referenceAnswer;
    private String aiFeedback;
    private Long courseId;
    private String courseName;
    private Long assignmentId;
    private String assignmentTitle;
    private LocalDateTime createTime;
}

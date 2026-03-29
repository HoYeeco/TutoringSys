package com.tutoring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpdateAssignmentRequest {

    @NotNull(message = "作业ID不能为空")
    private Long id;

    @NotBlank(message = "作业标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "截止时间不能为空")
    private LocalDateTime deadline;

    private Integer totalScore;

    private List<QuestionDTO> questions;
}

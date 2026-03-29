package com.tutoring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateCourseRequest {

    @NotNull(message = "课程ID不能为空")
    private Long id;

    @NotBlank(message = "课程名称不能为空")
    private String name;

    private String description;

    private List<Long> studentIds;
}

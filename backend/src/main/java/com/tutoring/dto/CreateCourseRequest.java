package com.tutoring.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateCourseRequest {

    @NotBlank(message = "课程名称不能为空")
    private String name;

    private String description;

    private List<Long> studentIds;
}

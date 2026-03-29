package com.tutoring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateCourseRequest {

    @NotBlank(message = "课程名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "教师ID不能为空")
    private Long teacherId;
}

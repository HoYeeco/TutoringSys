package com.tutoring.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateAvatarRequest {

    @NotBlank(message = "头像URL不能为空")
    private String avatarUrl;
}

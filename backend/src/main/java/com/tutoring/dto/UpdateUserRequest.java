package com.tutoring.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    private String realName;

    @Pattern(regexp = "STUDENT|TEACHER|ADMIN", message = "角色必须是STUDENT、TEACHER或ADMIN")
    private String role;

    private String avatar;

    private String email;

    private String phone;

    private Integer status;
}

package com.tutoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    private Long id;
    private String username;
    private String realName;
    private String role;
    private String avatar;
    private String email;
    private String phone;
    private Integer status;
    private LocalDateTime createTime;

}

package com.tutoring.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutoring.common.Result;
import com.tutoring.dto.LoginResponse;
import com.tutoring.entity.LoginRecord;
import com.tutoring.entity.User;
import com.tutoring.security.CustomUserDetailsService;
import com.tutoring.service.LoginRecordService;
import com.tutoring.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final LoginRecordService loginRecordService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.lambdaQuery()
            .eq(User::getUsername, userDetails.getUsername())
            .one();

        saveLoginRecord(user.getId(), request, true);

        LoginResponse loginResponse = LoginResponse.builder()
            .token(request.getSession().getId())
            .username(user.getUsername())
            .realName(user.getRealName())
            .role(user.getRole())
            .userId(user.getId())
            .build();

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.success(loginResponse)));
    }

    private void saveLoginRecord(Long userId, HttpServletRequest request, boolean success) {
        try {
            LoginRecord record = new LoginRecord();
            record.setUserId(userId);
            record.setLoginTime(LocalDateTime.now());
            record.setIpAddress(getClientIp(request));
            record.setUserAgent(request.getHeader("User-Agent"));
            record.setSuccess(success ? 1 : 0);
            loginRecordService.save(record);
        } catch (Exception e) {
            // 记录失败不影响登录流程
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

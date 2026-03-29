package com.tutoring.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutoring.common.Result;
import com.tutoring.entity.LoginRecord;
import com.tutoring.entity.User;
import com.tutoring.service.LoginRecordService;
import com.tutoring.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final LoginRecordService loginRecordService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("username");
        if (username == null) {
            username = request.getHeader("username");
        }
        
        if (username != null) {
            User user = userService.lambdaQuery()
                .eq(User::getUsername, username)
                .one();
            
            if (user != null) {
                saveLoginRecord(user.getId(), request, false);
            }
        }

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, "用户名或密码错误")));
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

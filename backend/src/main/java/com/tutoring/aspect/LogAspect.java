package com.tutoring.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutoring.annotation.Log;
import com.tutoring.entity.AuditLog;
import com.tutoring.service.AuditLogAsyncService;
import com.tutoring.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final AuditLogAsyncService auditLogAsyncService;
    private final ObjectMapper objectMapper;

    @Pointcut("@annotation(com.tutoring.annotation.Log)")
    public void logPointcut() {
    }

    @AfterReturning(pointcut = "logPointcut()", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Object jsonResult) {
        handleLog(joinPoint, null, jsonResult);
    }

    @AfterThrowing(pointcut = "logPointcut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        handleLog(joinPoint, e, null);
    }

    private void handleLog(JoinPoint joinPoint, Exception e, Object jsonResult) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Log logAnnotation = method.getAnnotation(Log.class);

            if (logAnnotation == null) {
                return;
            }

            AuditLog auditLog = new AuditLog();
            auditLog.setOperationTime(LocalDateTime.now());

            auditLog.setOperationType(logAnnotation.module() + "-" + logAnnotation.operation());
            auditLog.setOperationContent(logAnnotation.value());

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                auditLog.setOperator(authentication.getName());
                auditLog.setOperatorRole(SecurityUtil.getCurrentUserRole());
            } else {
                auditLog.setOperator("anonymous");
                auditLog.setOperatorRole("ANONYMOUS");
            }

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                auditLog.setIpAddress(getIpAddress(request));
                
                if (logAnnotation.saveRequestData()) {
                    String requestInfo = buildRequestInfo(joinPoint, request);
                    if (requestInfo.length() > 2000) {
                        requestInfo = requestInfo.substring(0, 2000);
                    }
                    auditLog.setOperationContent(auditLog.getOperationContent() + " | 请求: " + requestInfo);
                }
            }

            if (e != null) {
                auditLog.setSuccess(0);
                String errorMsg = e.getMessage();
                if (errorMsg != null && errorMsg.length() > 500) {
                    errorMsg = errorMsg.substring(0, 500);
                }
                auditLog.setErrorMessage(errorMsg);
            } else {
                auditLog.setSuccess(1);
                
                if (logAnnotation.saveResponseData() && jsonResult != null) {
                    try {
                        String responseStr = objectMapper.writeValueAsString(jsonResult);
                        if (responseStr.length() > 2000) {
                            responseStr = responseStr.substring(0, 2000);
                        }
                        auditLog.setOperationContent(auditLog.getOperationContent() + " | 响应: " + responseStr);
                    } catch (Exception ex) {
                        log.debug("序列化响应数据失败: {}", ex.getMessage());
                    }
                }
            }

            auditLogAsyncService.saveLog(auditLog);

        } catch (Exception ex) {
            log.error("记录操作日志异常: {}", ex.getMessage(), ex);
        }
    }

    private String buildRequestInfo(JoinPoint joinPoint, HttpServletRequest request) {
        try {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            
            Map<String, Object> requestInfo = new HashMap<>();
            requestInfo.put("method", method);
            requestInfo.put("uri", uri);
            
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                Map<String, Object> params = new HashMap<>();
                String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
                for (int i = 0; i < args.length; i++) {
                    if (paramNames != null && i < paramNames.length) {
                        Object arg = args[i];
                        if (arg instanceof HttpServletRequest 
                            || arg instanceof HttpServletResponse 
                            || arg instanceof MultipartFile) {
                            continue;
                        }
                        params.put(paramNames[i], arg);
                    }
                }
                if (!params.isEmpty()) {
                    requestInfo.put("params", params);
                }
            }
            
            return objectMapper.writeValueAsString(requestInfo);
        } catch (Exception e) {
            return "获取请求信息失败: " + e.getMessage();
        }
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
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

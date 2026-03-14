package com.tutoringsys.service.aspect;

import com.tutoringsys.dao.service.AuditLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Aspect
@Component
public class AuditLogAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);

    @Resource
    private AuditLogService auditLogService;

    // 定义切点，拦截控制器中的方法
    @Pointcut("execution(* com.tutoringsys.api.controller..*.*(..))")
    public void controllerPointcut() {
    }

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取请求信息
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ipAddress = request.getRemoteAddr();
        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        // 获取操作人信息（实际项目中从JWT token中获取）
        String operator = "admin"; // 模拟操作人
        String operatorRole = "ADMIN"; // 模拟操作人角色

        // 构建操作内容
        String operationContent = String.format("%s %s, 参数: %s", method, requestUri, Arrays.toString(joinPoint.getArgs()));

        boolean success = true;
        String errorMessage = null;
        Object result = null;

        try {
            // 执行目标方法
            result = joinPoint.proceed();
        } catch (Exception e) {
            success = false;
            errorMessage = e.getMessage();
            log.error("操作异常: {}", e.getMessage(), e);
            throw e;
        } finally {
            // 记录审计日志
            auditLogService.recordLog(
                    method,
                    operationContent,
                    operator,
                    operatorRole,
                    ipAddress,
                    success,
                    errorMessage
            );
        }

        return result;
    }
}
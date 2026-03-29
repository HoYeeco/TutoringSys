package com.tutoring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.*;
import com.tutoring.entity.*;
import com.tutoring.mapper.*;
import com.tutoring.service.AdminMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMonitorServiceImpl implements AdminMonitorService {

    private final LlmCallLogMapper llmCallLogMapper;
    private final AuditLogMapper auditLogMapper;
    private final LoginRecordMapper loginRecordMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public LLMMonitorVO getLLMMonitor() {
        Long totalCalls = llmCallLogMapper.selectCount(
            new LambdaQueryWrapper<LlmCallLog>()
        );

        Long successCalls = llmCallLogMapper.selectCount(
            new LambdaQueryWrapper<LlmCallLog>()
                .eq(LlmCallLog::getStatus, 1)
        );

        Long failedCalls = totalCalls - successCalls;

        Double successRate = totalCalls > 0 
            ? Math.round(successCalls * 100.0 / totalCalls * 100) / 100.0 
            : 0.0;

        List<LlmCallLog> allLogs = llmCallLogMapper.selectList(
            new LambdaQueryWrapper<LlmCallLog>()
        );

        Long totalPromptTokens = allLogs.stream()
            .filter(log -> log.getPromptTokens() != null)
            .mapToLong(LlmCallLog::getPromptTokens)
            .sum();

        Long totalCompletionTokens = allLogs.stream()
            .filter(log -> log.getCompletionTokens() != null)
            .mapToLong(LlmCallLog::getCompletionTokens)
            .sum();

        Long totalTokens = allLogs.stream()
            .filter(log -> log.getTotalTokens() != null)
            .mapToLong(LlmCallLog::getTotalTokens)
            .sum();

        List<LlmCallLog> failedLogs = llmCallLogMapper.selectList(
            new LambdaQueryWrapper<LlmCallLog>()
                .eq(LlmCallLog::getStatus, 0)
                .orderByDesc(LlmCallLog::getRequestTime)
                .last("LIMIT 10")
        );

        Set<Long> userIds = failedLogs.stream()
            .map(LlmCallLog::getUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        Map<Long, User> userMap = userIds.isEmpty() ? Map.of() :
            userMapper.selectList(
                new LambdaQueryWrapper<User>()
                    .in(User::getId, userIds)
            ).stream().collect(Collectors.toMap(User::getId, Function.identity()));

        List<LLMMonitorVO.FailedCallRecord> recentFailures = failedLogs.stream()
            .map(log -> {
                User user = log.getUserId() != null ? userMap.get(log.getUserId()) : null;
                return LLMMonitorVO.FailedCallRecord.builder()
                    .id(log.getId())
                    .requestId(log.getRequestId())
                    .userId(log.getUserId())
                    .username(user != null ? user.getUsername() : "")
                    .modelName(log.getModelName())
                    .errorMessage(log.getErrorMessage())
                    .requestTime(log.getRequestTime() != null ? log.getRequestTime().toString() : "")
                    .build();
            })
            .collect(Collectors.toList());

        return LLMMonitorVO.builder()
            .totalCalls(totalCalls)
            .successCalls(successCalls)
            .failedCalls(failedCalls)
            .successRate(successRate)
            .totalPromptTokens(totalPromptTokens)
            .totalCompletionTokens(totalCompletionTokens)
            .totalTokens(totalTokens)
            .recentFailures(recentFailures)
            .build();
    }

    @Override
    public RedisMonitorVO getRedisMonitor() {
        try {
            Properties info = redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<Properties>) connection -> {
                return connection.info();
            });

            if (info == null) {
                return RedisMonitorVO.builder()
                    .version("Unknown")
                    .hitRate(0.0)
                    .build();
            }

            String version = info.getProperty("redis_version", "Unknown");
            String mode = info.getProperty("redis_mode", "standalone");
            long uptimeInSeconds = parseLong(info.getProperty("uptime_in_seconds", "0"));
            long connectedClients = parseLong(info.getProperty("connected_clients", "0"));
            long totalConnectionsReceived = parseLong(info.getProperty("total_connections_received", "0"));
            long totalCommandsProcessed = parseLong(info.getProperty("total_commands_processed", "0"));
            long keyspaceHits = parseLong(info.getProperty("keyspace_hits", "0"));
            long keyspaceMisses = parseLong(info.getProperty("keyspace_misses", "0"));
            String usedMemory = info.getProperty("used_memory", "0");
            String usedMemoryPeak = info.getProperty("used_memory_peak", "0");
            String usedMemoryHuman = info.getProperty("used_memory_human", "0B");

            double hitRate = (keyspaceHits + keyspaceMisses) > 0 
                ? Math.round(keyspaceHits * 100.0 / (keyspaceHits + keyspaceMisses) * 100) / 100.0 
                : 0.0;

            Long dbSize = redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<Long>) connection -> {
                Long size = connection.dbSize();
                return size != null ? size : 0L;
            });

            return RedisMonitorVO.builder()
                .version(version)
                .mode(mode)
                .uptimeInSeconds(uptimeInSeconds)
                .connectedClients(connectedClients)
                .totalConnectionsReceived(totalConnectionsReceived)
                .totalCommandsProcessed(totalCommandsProcessed)
                .keyspaceHits(keyspaceHits)
                .keyspaceMisses(keyspaceMisses)
                .hitRate(hitRate)
                .usedMemory(usedMemory)
                .usedMemoryPeak(usedMemoryPeak)
                .usedMemoryHuman(usedMemoryHuman)
                .dbSize(dbSize)
                .build();
        } catch (Exception e) {
            log.error("Failed to get Redis info", e);
            return RedisMonitorVO.builder()
                .version("Error: " + e.getMessage())
                .hitRate(0.0)
                .build();
        }
    }

    @Override
    public Page<AuditLogVO> listAuditLogs(Integer page, Integer size, String operator,
            String operationType, LocalDateTime startTime, LocalDateTime endTime) {

        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(operator)) {
            wrapper.like(AuditLog::getOperator, operator);
        }
        if (StringUtils.hasText(operationType)) {
            wrapper.eq(AuditLog::getOperationType, operationType);
        }
        if (startTime != null) {
            wrapper.ge(AuditLog::getOperationTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(AuditLog::getOperationTime, endTime);
        }

        wrapper.orderByDesc(AuditLog::getOperationTime);

        Page<AuditLog> logPage = auditLogMapper.selectPage(new Page<>(page, size), wrapper);

        List<AuditLogVO> voList = logPage.getRecords().stream()
            .map(log -> AuditLogVO.builder()
                .id(log.getId())
                .operationType(log.getOperationType())
                .operationContent(log.getOperationContent())
                .operator(log.getOperator())
                .operatorRole(log.getOperatorRole())
                .ipAddress(log.getIpAddress())
                .success(log.getSuccess())
                .errorMessage(log.getErrorMessage())
                .operationTime(log.getOperationTime())
                .build())
            .collect(Collectors.toList());

        Page<AuditLogVO> voPage = new Page<>(logPage.getCurrent(), logPage.getSize(), logPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Page<LoginRecordVO> listLoginRecords(Integer page, Integer size, Long userId,
            LocalDateTime startTime, LocalDateTime endTime) {

        LambdaQueryWrapper<LoginRecord> wrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            wrapper.eq(LoginRecord::getUserId, userId);
        }
        if (startTime != null) {
            wrapper.ge(LoginRecord::getLoginTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(LoginRecord::getLoginTime, endTime);
        }

        wrapper.orderByDesc(LoginRecord::getLoginTime);

        Page<LoginRecord> recordPage = loginRecordMapper.selectPage(new Page<>(page, size), wrapper);

        Set<Long> userIds = recordPage.getRecords().stream()
            .map(LoginRecord::getUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        Map<Long, User> userMap = userIds.isEmpty() ? Map.of() :
            userMapper.selectList(
                new LambdaQueryWrapper<User>()
                    .in(User::getId, userIds)
            ).stream().collect(Collectors.toMap(User::getId, Function.identity()));

        List<LoginRecordVO> voList = recordPage.getRecords().stream()
            .map(record -> {
                User user = userMap.get(record.getUserId());
                return LoginRecordVO.builder()
                    .id(record.getId())
                    .userId(record.getUserId())
                    .username(user != null ? user.getUsername() : "")
                    .realName(user != null ? user.getRealName() : "")
                    .role(user != null ? user.getRole() : "")
                    .loginTime(record.getLoginTime())
                    .ipAddress(record.getIpAddress())
                    .userAgent(record.getUserAgent())
                    .success(record.getSuccess())
                    .build();
            })
            .collect(Collectors.toList());

        Page<LoginRecordVO> voPage = new Page<>(recordPage.getCurrent(), recordPage.getSize(), recordPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

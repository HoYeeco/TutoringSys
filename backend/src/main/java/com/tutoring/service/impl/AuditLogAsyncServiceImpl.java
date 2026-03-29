package com.tutoring.service.impl;

import com.tutoring.entity.AuditLog;
import com.tutoring.mapper.AuditLogMapper;
import com.tutoring.service.AuditLogAsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogAsyncServiceImpl implements AuditLogAsyncService {

    private final AuditLogMapper auditLogMapper;

    @Override
    @Async("taskExecutor")
    public void saveLog(AuditLog auditLog) {
        try {
            auditLogMapper.insert(auditLog);
            log.debug("异步保存操作日志成功: {}", auditLog.getOperationContent());
        } catch (Exception e) {
            log.error("保存操作日志失败: {}", e.getMessage(), e);
        }
    }

}

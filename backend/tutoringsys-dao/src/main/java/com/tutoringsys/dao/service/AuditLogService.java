package com.tutoringsys.dao.service;

import com.tutoringsys.dao.entity.AuditLog;
import com.tutoringsys.dao.mapper.AuditLogMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class AuditLogService {

    @Resource
    private AuditLogMapper auditLogMapper;

    /**
     * 记录审计日志
     * @param operationType 操作类型
     * @param operationContent 操作内容
     * @param operator 操作人
     * @param operatorRole 操作人角色
     * @param ipAddress IP地址
     * @param success 是否成功
     * @param errorMessage 错误信息
     */
    public void recordLog(String operationType, String operationContent, String operator, String operatorRole, String ipAddress, boolean success, String errorMessage) {
        AuditLog auditLog = new AuditLog();
        auditLog.setOperationType(operationType);
        auditLog.setOperationContent(operationContent);
        auditLog.setOperator(operator);
        auditLog.setOperatorRole(operatorRole);
        auditLog.setIpAddress(ipAddress);
        auditLog.setSuccess(success);
        auditLog.setErrorMessage(errorMessage);
        auditLog.setOperationTime(new Date());

        auditLogMapper.insert(auditLog);
    }
}
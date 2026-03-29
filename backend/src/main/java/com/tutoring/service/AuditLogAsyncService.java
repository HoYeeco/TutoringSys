package com.tutoring.service;

import com.tutoring.entity.AuditLog;

public interface AuditLogAsyncService {

    void saveLog(AuditLog auditLog);

}

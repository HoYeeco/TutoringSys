package com.tutoring.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.AuditLogVO;
import com.tutoring.dto.LLMMonitorVO;
import com.tutoring.dto.LoginRecordVO;
import com.tutoring.dto.RedisMonitorVO;

import java.time.LocalDateTime;

public interface AdminMonitorService {

    LLMMonitorVO getLLMMonitor();

    RedisMonitorVO getRedisMonitor();

    Page<AuditLogVO> listAuditLogs(Integer page, Integer size, String operator, 
            String operationType, LocalDateTime startTime, LocalDateTime endTime);

    Page<LoginRecordVO> listLoginRecords(Integer page, Integer size, Long userId, 
            LocalDateTime startTime, LocalDateTime endTime);
}

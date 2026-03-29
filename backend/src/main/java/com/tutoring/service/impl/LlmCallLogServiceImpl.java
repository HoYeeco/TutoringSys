package com.tutoring.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tutoring.entity.LlmCallLog;
import com.tutoring.mapper.LlmCallLogMapper;
import com.tutoring.service.LlmCallLogService;
import org.springframework.stereotype.Service;

@Service
public class LlmCallLogServiceImpl extends ServiceImpl<LlmCallLogMapper, LlmCallLog> implements LlmCallLogService {
}

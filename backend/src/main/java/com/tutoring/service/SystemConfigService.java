package com.tutoring.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tutoring.entity.SystemConfig;

public interface SystemConfigService extends IService<SystemConfig> {
    
    SystemConfig getByKey(String configKey);
}

package com.tutoring.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tutoring.entity.SystemConfig;
import com.tutoring.mapper.SystemConfigMapper;
import com.tutoring.service.SystemConfigService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    @Override
    @Cacheable(value = "systemConfig", key = "#id")
    public SystemConfig getById(Serializable id) {
        return super.getById(id);
    }

    @Override
    @Cacheable(value = "systemConfig", key = "'key:' + #configKey")
    public SystemConfig getByKey(String configKey) {
        return lambdaQuery()
            .eq(SystemConfig::getConfigKey, configKey)
            .one();
    }

    @Override
    @CacheEvict(value = "systemConfig", allEntries = true)
    public boolean save(SystemConfig entity) {
        return super.save(entity);
    }

    @Override
    @CacheEvict(value = "systemConfig", allEntries = true)
    public boolean updateById(SystemConfig entity) {
        return super.updateById(entity);
    }

    @Override
    @CacheEvict(value = "systemConfig", allEntries = true)
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }
}

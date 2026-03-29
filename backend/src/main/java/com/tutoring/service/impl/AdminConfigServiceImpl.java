package com.tutoring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.CreateConfigRequest;
import com.tutoring.dto.SystemConfigVO;
import com.tutoring.dto.UpdateConfigRequest;
import com.tutoring.entity.SystemConfig;
import com.tutoring.mapper.SystemConfigMapper;
import com.tutoring.service.AdminConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminConfigServiceImpl implements AdminConfigService {

    private final SystemConfigMapper systemConfigMapper;

    private static final String CACHE_NAME = "systemConfig";

    @Override
    public Page<SystemConfigVO> listConfigs(Integer page, Integer size, String configType, String keyword) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(configType)) {
            wrapper.eq(SystemConfig::getConfigType, configType);
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(SystemConfig::getConfigKey, keyword)
                .or().like(SystemConfig::getDescription, keyword)
            );
        }

        wrapper.orderByDesc(SystemConfig::getCreateTime);

        Page<SystemConfig> configPage = systemConfigMapper.selectPage(new Page<>(page, size), wrapper);

        Page<SystemConfigVO> voPage = new Page<>(configPage.getCurrent(), configPage.getSize(), configPage.getTotal());
        voPage.setRecords(configPage.getRecords().stream()
            .map(this::convertToVO)
            .toList());

        return voPage;
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "#id")
    public SystemConfigVO getConfigById(Long id) {
        log.info("从数据库查询配置: id={}", id);
        SystemConfig config = systemConfigMapper.selectById(id);
        return config != null ? convertToVO(config) : null;
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'key:' + #configKey")
    public SystemConfigVO getConfigByKey(String configKey) {
        log.info("从数据库查询配置: key={}", configKey);
        SystemConfig config = systemConfigMapper.selectOne(
            new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, configKey)
        );
        return config != null ? convertToVO(config) : null;
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public SystemConfigVO createConfig(CreateConfigRequest request) {
        SystemConfig existing = systemConfigMapper.selectOne(
            new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, request.getConfigKey())
        );

        if (existing != null) {
            throw new RuntimeException("配置键已存在: " + request.getConfigKey());
        }

        SystemConfig config = new SystemConfig();
        config.setConfigKey(request.getConfigKey());
        config.setConfigValue(request.getConfigValue());
        config.setConfigType(request.getConfigType());
        config.setDescription(request.getDescription());

        systemConfigMapper.insert(config);

        return convertToVO(config);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public SystemConfigVO updateConfig(Long id, UpdateConfigRequest request) {
        SystemConfig config = systemConfigMapper.selectById(id);
        if (config == null) {
            throw new RuntimeException("配置不存在");
        }

        if (request.getConfigValue() != null) {
            config.setConfigValue(request.getConfigValue());
        }
        if (request.getConfigType() != null) {
            config.setConfigType(request.getConfigType());
        }
        if (request.getDescription() != null) {
            config.setDescription(request.getDescription());
        }

        systemConfigMapper.updateById(config);

        return convertToVO(config);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void deleteConfig(Long id) {
        SystemConfig config = systemConfigMapper.selectById(id);
        if (config == null) {
            throw new RuntimeException("配置不存在");
        }

        systemConfigMapper.deleteById(id);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void clearCache(String prefix) {
        log.info("清除系统配置缓存");
    }

    @Override
    public String getConfigValue(String configKey) {
        SystemConfigVO config = getConfigByKey(configKey);
        return config != null ? config.getConfigValue() : null;
    }

    private SystemConfigVO convertToVO(SystemConfig config) {
        return SystemConfigVO.builder()
            .id(config.getId())
            .configKey(config.getConfigKey())
            .configValue(config.getConfigValue())
            .configType(config.getConfigType())
            .description(config.getDescription())
            .createTime(config.getCreateTime())
            .updateTime(config.getUpdateTime())
            .build();
    }
}

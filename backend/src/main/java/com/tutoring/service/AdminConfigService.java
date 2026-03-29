package com.tutoring.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.CreateConfigRequest;
import com.tutoring.dto.SystemConfigVO;
import com.tutoring.dto.UpdateConfigRequest;

public interface AdminConfigService {

    Page<SystemConfigVO> listConfigs(Integer page, Integer size, String configType, String keyword);

    SystemConfigVO getConfigById(Long id);

    SystemConfigVO getConfigByKey(String configKey);

    SystemConfigVO createConfig(CreateConfigRequest request);

    SystemConfigVO updateConfig(Long id, UpdateConfigRequest request);

    void deleteConfig(Long id);

    void clearCache(String prefix);

    String getConfigValue(String configKey);
}

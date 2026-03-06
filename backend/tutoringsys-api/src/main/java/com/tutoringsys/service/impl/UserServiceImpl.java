package com.tutoringsys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoringsys.api.dto.*;
import com.tutoringsys.dao.entity.User;
import com.tutoringsys.dao.mapper.UserMapper;
import com.tutoringsys.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public IPage<AdminUserVo> pageUsers(int page, int size, Map<String, Object> params) {
        Page<User> userPage = new Page<>(page, size);
        // 简化实现
        return new Page<>();
    }

    @Override
    public User createUser(AdminUserCreateDto dto) {
        // 简化实现
        return new User();
    }

    @Override
    public User updateUser(Long id, AdminUserUpdateDto dto) {
        // 简化实现
        return new User();
    }

    @Override
    public void deleteUser(Long id) {
        // 简化实现
    }

    @Override
    public Map<String, String> resetPassword(Long id) {
        // 简化实现
        return new HashMap<>();
    }

    @Override
    public User getUserByUsername(String username) {
        // 简化实现
        return new User();
    }
}

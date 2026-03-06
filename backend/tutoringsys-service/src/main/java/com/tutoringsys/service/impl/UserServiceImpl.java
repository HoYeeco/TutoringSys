package com.tutoringsys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoringsys.common.dto.AdminUserCreateDto;
import com.tutoringsys.common.dto.AdminUserUpdateDto;
import com.tutoringsys.common.dto.AdminUserVo;
import com.tutoringsys.common.exception.BusinessException;
import com.tutoringsys.dao.entity.User;
import com.tutoringsys.dao.mapper.UserMapper;
import com.tutoringsys.service.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public IPage<AdminUserVo> pageUsers(int page, int size, Map<String, Object> params) {
        Page<User> userPage = new Page<>(page, size);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        if (params.containsKey("username")) {
            queryWrapper.like(User::getUsername, params.get("username"));
        }
        if (params.containsKey("role")) {
            queryWrapper.eq(User::getRole, params.get("role"));
        }
        if (params.containsKey("status")) {
            queryWrapper.eq(User::getStatus, params.get("status"));
        }
        queryWrapper.eq(User::getDeleted, 0);

        IPage<User> users = userMapper.selectPage(userPage, queryWrapper);
        return users.convert(user -> {
            AdminUserVo vo = new AdminUserVo();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
            vo.setRole(user.getRole());
            vo.setAvatar(user.getAvatar());
            vo.setEmail(user.getEmail());
            vo.setPhone(user.getPhone());
            vo.setStatus(user.getStatus());
            vo.setCreateTime(user.getCreateTime());
            vo.setUpdateTime(user.getUpdateTime());
            return vo;
        });
    }

    @Override
    public User createUser(AdminUserCreateDto dto) {
        // 校验用户名唯一性
        if (getUserByUsername(dto.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setRole(dto.getRole());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(dto.getStatus());
        user.setDeleted(0);

        userMapper.insert(user);
        return user;
    }

    @Override
    public User updateUser(Long id, AdminUserUpdateDto dto) {
        User user = getUserById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (dto.getRealName() != null) {
            user.setRealName(dto.getRealName());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }
        if (dto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        userMapper.updateById(user);
        return user;
    }

    @Override
    public void deleteUser(Long id) {
        User user = getUserById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查用户是否有关联数据
        // 这里可以添加检查逻辑，如是否有课程、作业等

        user.setDeleted(1);
        userMapper.updateById(user);
    }

    @Override
    public Map<String, String> resetPassword(Long id) {
        User user = getUserById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        String newPassword = RandomStringUtils.randomAlphanumeric(8);
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        Map<String, String> result = new HashMap<>();
        result.put("password", newPassword);
        return result;
    }

    @Override
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return userMapper.selectOne(queryWrapper);
    }
}

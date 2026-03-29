package com.tutoring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.CreateUserRequest;
import com.tutoring.dto.UpdateUserRequest;
import com.tutoring.dto.UserVO;
import com.tutoring.entity.User;
import com.tutoring.mapper.UserMapper;
import com.tutoring.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<UserVO> listUsers(Integer page, Integer size, String role, Integer status, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        }

        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(User::getUsername, keyword)
                .or().like(User::getRealName, keyword)
                .or().like(User::getEmail, keyword)
                .or().like(User::getPhone, keyword)
            );
        }

        wrapper.orderByDesc(User::getCreateTime);

        Page<User> userPage = userMapper.selectPage(new Page<>(page, size), wrapper);

        Page<UserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> voList = userPage.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public UserVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return null;
        }
        return convertToVO(user);
    }

    @Override
    public UserVO createUser(CreateUserRequest request) {
        User existingUser = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
        );
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setRole(request.getRole());
        user.setAvatar(request.getAvatar());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);

        userMapper.insert(user);

        return convertToVO(user);
    }

    @Override
    public UserVO updateUser(Long id, UpdateUserRequest request) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        userMapper.updateById(user);

        return convertToVO(user);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if ("ADMIN".equals(user.getRole())) {
            long adminCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                    .eq(User::getRole, "ADMIN")
            );
            if (adminCount <= 1) {
                throw new RuntimeException("不能删除最后一个管理员账户");
            }
        }

        userMapper.deleteById(id);
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if ("ADMIN".equals(user.getRole()) && status == 0) {
            long activeAdminCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                    .eq(User::getRole, "ADMIN")
                    .eq(User::getStatus, 1)
            );
            if (activeAdminCount <= 1) {
                throw new RuntimeException("不能禁用最后一个活跃的管理员账户");
            }
        }

        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
        );
    }

    private UserVO convertToVO(User user) {
        return UserVO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .realName(user.getRealName())
            .role(user.getRole())
            .avatar(user.getAvatar())
            .email(user.getEmail())
            .phone(user.getPhone())
            .status(user.getStatus())
            .createTime(user.getCreateTime())
            .updateTime(user.getUpdateTime())
            .build();
    }
}

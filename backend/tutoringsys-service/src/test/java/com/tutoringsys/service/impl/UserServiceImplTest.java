package com.tutoringsys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tutoringsys.dao.entity.User;
import com.tutoringsys.dao.mapper.UserMapper;
import com.tutoringsys.service.dto.AdminUserCreateDto;
import com.tutoringsys.service.dto.LoginDto;
import com.tutoringsys.service.dto.UserLoginVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister() {
        // 准备测试数据
        AdminUserCreateDto dto = new AdminUserCreateDto();
        dto.setUsername("testuser");
        dto.setPassword("123456");
        dto.setEmail("test@example.com");
        dto.setRole("STUDENT");

        // 模拟密码加密
        when(passwordEncoder.encode(anyString())).thenReturn("encryptedPassword");

        // 模拟用户不存在
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        // 执行测试
        boolean result = userService.register(dto);

        // 验证结果
        assertTrue(result);
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    void testLogin() {
        // 准备测试数据
        LoginDto dto = new LoginDto();
        dto.setUsername("testuser");
        dto.setPassword("123456");

        // 模拟用户存在
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encryptedPassword");
        user.setRole("STUDENT");

        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // 执行测试
        UserLoginVo result = userService.login(dto);

        // 验证结果
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertNotNull(result.getToken());
    }

    @Test
    void testResetPassword() {
        // 准备测试数据
        Long userId = 1L;
        String newPassword = "654321";

        // 模拟密码加密
        when(passwordEncoder.encode(anyString())).thenReturn("encryptedNewPassword");

        // 模拟用户存在
        User user = new User();
        user.setId(userId);
        when(userMapper.selectById(userId)).thenReturn(user);

        // 执行测试
        boolean result = userService.resetPassword(userId, newPassword);

        // 验证结果
        assertTrue(result);
        verify(userMapper, times(1)).updateById(any(User.class));
    }
}

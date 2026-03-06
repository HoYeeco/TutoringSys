package com.tutoringsys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutoringsys.api.dto.*;
import com.tutoringsys.dao.entity.User;

import java.util.Map;

public interface UserService {
    IPage<AdminUserVo> pageUsers(int page, int size, Map<String, Object> params);
    User createUser(AdminUserCreateDto dto);
    User updateUser(Long id, AdminUserUpdateDto dto);
    void deleteUser(Long id);
    Map<String, String> resetPassword(Long id);
    User getUserByUsername(String username);
}

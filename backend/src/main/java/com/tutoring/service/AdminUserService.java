package com.tutoring.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.CreateUserRequest;
import com.tutoring.dto.UpdateUserRequest;
import com.tutoring.dto.UserVO;
import com.tutoring.entity.User;

public interface AdminUserService {

    Page<UserVO> listUsers(Integer page, Integer size, String role, Integer status, String keyword);

    UserVO getUserById(Long id);

    UserVO createUser(CreateUserRequest request);

    UserVO updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);

    void updateUserStatus(Long id, Integer status);

    User getByUsername(String username);
}

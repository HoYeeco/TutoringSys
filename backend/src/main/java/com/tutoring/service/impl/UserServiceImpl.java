package com.tutoring.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tutoring.entity.User;
import com.tutoring.mapper.UserMapper;
import com.tutoring.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}

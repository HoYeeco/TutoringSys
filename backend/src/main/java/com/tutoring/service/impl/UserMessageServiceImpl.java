package com.tutoring.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tutoring.entity.UserMessage;
import com.tutoring.mapper.UserMessageMapper;
import com.tutoring.service.UserMessageService;
import org.springframework.stereotype.Service;

@Service
public class UserMessageServiceImpl extends ServiceImpl<UserMessageMapper, UserMessage> implements UserMessageService {
}

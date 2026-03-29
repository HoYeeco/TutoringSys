package com.tutoring.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tutoring.entity.LoginRecord;
import com.tutoring.mapper.LoginRecordMapper;
import com.tutoring.service.LoginRecordService;
import org.springframework.stereotype.Service;

@Service
public class LoginRecordServiceImpl extends ServiceImpl<LoginRecordMapper, LoginRecord> implements LoginRecordService {
}

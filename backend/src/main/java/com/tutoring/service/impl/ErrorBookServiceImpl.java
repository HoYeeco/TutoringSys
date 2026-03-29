package com.tutoring.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tutoring.entity.ErrorBook;
import com.tutoring.mapper.ErrorBookMapper;
import com.tutoring.service.ErrorBookService;
import org.springframework.stereotype.Service;

@Service
public class ErrorBookServiceImpl extends ServiceImpl<ErrorBookMapper, ErrorBook> implements ErrorBookService {
}

package com.tutoring.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tutoring.entity.UserMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMessageMapper extends BaseMapper<UserMessage> {
}

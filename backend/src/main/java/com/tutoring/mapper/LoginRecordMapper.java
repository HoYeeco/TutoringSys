package com.tutoring.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tutoring.entity.LoginRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginRecordMapper extends BaseMapper<LoginRecord> {
}

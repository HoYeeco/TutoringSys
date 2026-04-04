package com.tutoring.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tutoring.entity.Submission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface SubmissionMapper extends BaseMapper<Submission> {

    @Select("SELECT COUNT(*) FROM submission WHERE student_id = #{studentId} AND final_total_score IS NOT NULL")
    Long countGradedByStudentId(Long studentId);

    @Select("SELECT COUNT(*) FROM submission WHERE student_id = #{studentId} AND (status IN (0, 1, 2) OR (status = 3 AND review_status = 1))")
    Long countPendingGradingByStudentId(Long studentId);

}

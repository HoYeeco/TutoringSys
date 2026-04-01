package com.tutoring.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tutoring.entity.StudentAnswer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface StudentAnswerMapper extends BaseMapper<StudentAnswer> {

    @Update("UPDATE student_answer SET is_draft = 1, submission_id = NULL WHERE submission_id = #{submissionId}")
    int updateStatusToDraft(@Param("submissionId") Long submissionId);
}

package com.tutoring.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tutoring.entity.CourseSelection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CourseSelectionMapper extends BaseMapper<CourseSelection> {

    @Select("SELECT COUNT(*) FROM course_selection WHERE student_id = #{studentId}")
    Long selectCountByStudentId(Long studentId);

}

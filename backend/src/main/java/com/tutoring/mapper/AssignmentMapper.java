package com.tutoring.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tutoring.entity.Assignment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AssignmentMapper extends BaseMapper<Assignment> {

    @Select("SELECT COUNT(*) FROM assignment a " +
            "WHERE a.course_id IN (SELECT cs.course_id FROM course_selection cs WHERE cs.student_id = #{studentId}) " +
            "AND a.deadline > NOW() " +
            "AND a.id NOT IN (SELECT s.assignment_id FROM submission s WHERE s.student_id = #{studentId})")
    Long countPendingByStudentId(Long studentId);

}

package com.tutoringsys.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("course_student")
public class CourseStudent {
    private Long id;
    private Long courseId;
    private Long studentId;
}
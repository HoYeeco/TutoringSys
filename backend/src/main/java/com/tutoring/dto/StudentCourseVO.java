package com.tutoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentCourseVO {

    private Long id;
    private String name;
    private String description;
    private Long teacherId;
    private String teacherName;
    private String teacherAvatar;
    private Integer assignmentCount;
    private LocalDateTime createTime;

}

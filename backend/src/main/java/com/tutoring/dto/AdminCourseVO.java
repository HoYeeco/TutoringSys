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
public class AdminCourseVO {

    private Long id;
    private String name;
    private String description;
    private Long teacherId;
    private String teacherName;
    private Integer studentCount;
    private Integer assignmentCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

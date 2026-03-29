package com.tutoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailVO {

    private Long id;
    private String name;
    private String description;
    private Long teacherId;
    private String teacherName;
    private LocalDateTime createTime;
    private List<StudentAssignmentVO> assignments;

}

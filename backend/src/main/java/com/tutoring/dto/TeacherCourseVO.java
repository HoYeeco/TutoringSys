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
public class TeacherCourseVO {

    private Long id;
    private String name;
    private String description;
    private Long teacherId;
    private String teacherName;
    private Integer studentCount;
    private Integer assignmentCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<StudentInfo> students;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentInfo {
        private Long id;
        private String username;
        private String realName;
    }
}

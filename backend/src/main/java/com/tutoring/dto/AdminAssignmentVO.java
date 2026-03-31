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
public class AdminAssignmentVO {

    private Long id;
    private String title;
    private String description;
    private Long courseId;
    private String courseName;
    private Long teacherId;
    private String teacherName;
    private String teacherAvatar;
    private LocalDateTime deadline;
    private Integer totalScore;
    private String status;
    private Integer questionCount;
    private Integer submissionCount;
    private Integer totalStudents;
    private Integer submittedCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<QuestionDetailDTO> questions;
}

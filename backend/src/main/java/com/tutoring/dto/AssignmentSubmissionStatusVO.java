package com.tutoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentSubmissionStatusVO {

    private Long assignmentId;
    private String assignmentTitle;
    private Integer totalStudents;
    private Integer submittedCount;
    private Integer notSubmittedCount;
    private List<StudentInfo> submittedStudents;
    private List<StudentInfo> notSubmittedStudents;
    private List<StudentInfo> allStudents;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentInfo {
        private Long studentId;
        private String studentName;
        private String username;
        private String avatar;
        private String email;
        private String phone;
    }
}

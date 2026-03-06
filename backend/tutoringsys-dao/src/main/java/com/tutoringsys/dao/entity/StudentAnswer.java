package com.tutoringsys.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("student_answer")
public class StudentAnswer {
    private Long id;
    private String submissionId;
    private Long studentId;
    private Long assignmentId;
    private Long questionId;
    private String answerContent;
    private Integer score;
    private String feedback;
    private String graderType;
    private Date createTime;
}
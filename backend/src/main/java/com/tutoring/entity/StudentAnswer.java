package com.tutoring.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("student_answer")
public class StudentAnswer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long submissionId;
    private Long studentId;
    private Long assignmentId;
    private Long questionId;
    private String answer;
    private String answerContent;
    private Integer score;
    private Integer aiScore;
    private Integer finalScore;
    private String aiFeedback;
    private String teacherFeedback;
    private String feedback;
    private String graderType;
    private Integer reviewStatus;
    private Long reviewerId;
    private LocalDateTime reviewTime;
    private Integer isDraft;
    private Integer status;
    private LocalDateTime submitTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @Version
    private Integer version;
}

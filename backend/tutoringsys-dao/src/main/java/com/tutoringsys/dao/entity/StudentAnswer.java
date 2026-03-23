package com.tutoringsys.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
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
    private String answer;
    private String answerContent;
    private String feedback;
    private String teacherFeedback;
    private String aiFeedback;
    private Integer isGraded;
    private Integer score;
    private Integer aiScore;
    private Integer finalScore;
    private Integer status;
    private Integer reviewStatus;
    private Long reviewerId;
    private String graderType;
    private Date submitTime;
    private Date reviewTime;
    private Date createTime;
    private Date updateTime;
    @Version
    private Integer version;
}

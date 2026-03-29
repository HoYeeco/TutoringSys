package com.tutoring.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("submission")
public class Submission {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String submissionId;
    private Long studentId;
    private Long assignmentId;
    private Integer status;
    private Integer totalScore;
    private Integer aiTotalScore;
    private Integer finalTotalScore;
    private Integer reviewStatus;
    private Long reviewerId;
    private LocalDateTime reviewTime;
    private LocalDateTime submitTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

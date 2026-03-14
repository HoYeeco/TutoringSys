package com.tutoringsys.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String type; // announcement, assignment, grading
    private Long relatedId;
    private String relatedType;
    private LocalDateTime createTime;
}

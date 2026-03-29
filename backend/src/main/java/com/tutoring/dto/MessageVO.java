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
public class MessageVO {

    private Long id;
    private Long messageId;
    private String title;
    private String content;
    private String type;
    private Long relatedId;
    private String relatedType;
    private Integer isRead;
    private LocalDateTime readTime;
    private LocalDateTime createTime;

}

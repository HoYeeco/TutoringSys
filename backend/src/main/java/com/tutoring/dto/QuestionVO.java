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
public class QuestionVO {

    private Long id;
    private String type;
    private String content;
    private List<String> options;
    private Integer score;
    private Integer minWords;
    private Integer maxWords;
    private Integer sortOrder;
    private String studentAnswer;
    private Long answerId;
}

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
public class QuestionDTO {

    private Long id;
    private String type;
    private String content;
    private List<String> options;
    private String answer;
    private String referenceAnswer;
    private Integer score;
    private Integer minWords;
    private Integer maxWords;
    private Integer sortOrder;
}

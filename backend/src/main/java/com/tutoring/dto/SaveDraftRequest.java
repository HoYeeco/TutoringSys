package com.tutoring.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SaveDraftRequest {

    @NotEmpty(message = "答案列表不能为空")
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        @NotNull(message = "题目ID不能为空")
        private Long questionId;
        private String answer;
    }
}

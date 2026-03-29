package com.tutoring.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReviewActionRequest {

    @NotNull(message = "答案ID不能为空")
    private Long answerId;

    private String action;

    private Integer newScore;

    private String teacherFeedback;

    public static class BatchRequest {
        @NotNull(message = "答案ID列表不能为空")
        private List<Long> answerIds;

        private String action;

        private String teacherFeedback;

        public List<Long> getAnswerIds() {
            return answerIds;
        }

        public void setAnswerIds(List<Long> answerIds) {
            this.answerIds = answerIds;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getTeacherFeedback() {
            return teacherFeedback;
        }

        public void setTeacherFeedback(String teacherFeedback) {
            this.teacherFeedback = teacherFeedback;
        }
    }
}

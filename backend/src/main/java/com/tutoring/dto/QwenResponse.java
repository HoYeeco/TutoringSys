package com.tutoring.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class QwenResponse {

    @JsonProperty("request_id")
    private String requestId;
    
    private Output output;
    
    private Usage usage;
    
    private String code;
    
    private String message;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Output {
        @JsonProperty("finish_reason")
        private String finishReason;
        
        private String text;
        
        private List<Choice> choices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private Message message;
        private Integer index;
        
        @JsonProperty("finish_reason")
        private String finishReason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        @JsonProperty("input_tokens")
        private Integer promptTokens;
        
        @JsonProperty("output_tokens")
        private Integer completionTokens;
        
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }

    public String getContent() {
        if (output != null) {
            if (output.getText() != null) {
                return output.getText();
            }
            if (output.getChoices() != null && !output.getChoices().isEmpty()) {
                Message message = output.getChoices().get(0).getMessage();
                if (message != null) {
                    return message.getContent();
                }
            }
        }
        return null;
    }

    public boolean isSuccess() {
        if (code != null && !"Success".equals(code) && !"200".equals(code)) {
            return false;
        }
        if (output != null) {
            // 检查 choices 数组中的 finishReason
            if (output.getChoices() != null && !output.getChoices().isEmpty()) {
                String finishReason = output.getChoices().get(0).getFinishReason();
                if ("stop".equals(finishReason)) {
                    return true;
                }
            }
            // 兼容旧版本：检查 output 的 finishReason
            if ("stop".equals(output.getFinishReason())) {
                return true;
            }
            // 如果有文本内容，也认为成功
            if (output.getText() != null) {
                return true;
            }
        }
        return false;
    }
}

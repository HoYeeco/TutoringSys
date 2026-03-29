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
public class QwenRequest {

    private String model;
    private Input input;
    private Parameters parameters;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Input {
        private List<Message> messages;
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
    public static class Parameters {
        private String resultFormat;
    }

    public static QwenRequest createChatRequest(String model, String systemPrompt, String userMessage) {
        return QwenRequest.builder()
            .model(model)
            .input(Input.builder()
                .messages(List.of(
                    Message.builder().role("system").content(systemPrompt).build(),
                    Message.builder().role("user").content(userMessage).build()
                ))
                .build())
            .parameters(Parameters.builder().resultFormat("message").build())
            .build();
    }
}

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
public class GradingResult {

    private Integer score;
    private List<String> errors;
    private List<String> suggestions;
    private String feedback;

    public static GradingResult parseFromJson(String jsonContent) {
        if (jsonContent == null || jsonContent.isEmpty()) {
            return GradingResult.builder()
                .score(0)
                .errors(List.of())
                .suggestions(List.of())
                .feedback("无法解析AI响应")
                .build();
        }

        try {
            String content = jsonContent;
            if (content.contains("```json")) {
                int start = content.indexOf("```json") + 7;
                int end = content.indexOf("```", start);
                if (end > start) {
                    content = content.substring(start, end).trim();
                }
            } else if (content.contains("```")) {
                int start = content.indexOf("```") + 3;
                int end = content.indexOf("```", start);
                if (end > start) {
                    content = content.substring(start, end).trim();
                }
            }

            content = content.replaceAll("^[^{\\[]*", "").replaceAll("[^}\\]]*$", "");

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(content, GradingResult.class);
        } catch (Exception e) {
            return GradingResult.builder()
                .score(0)
                .errors(List.of("解析AI响应失败: " + e.getMessage()))
                .suggestions(List.of())
                .feedback(jsonContent)
                .build();
        }
    }
}

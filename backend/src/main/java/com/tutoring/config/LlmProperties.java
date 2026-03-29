package com.tutoring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {

    private String apiKey;
    private String model;
    private Integer timeout;
    private Integer retry;

}

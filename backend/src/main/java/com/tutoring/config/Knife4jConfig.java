package com.tutoring.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tutoring System API")
                        .description("Tutoring System Backend API Documentation")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Tutoring Team")
                                .email("contact@tutoring.com")));
    }

}

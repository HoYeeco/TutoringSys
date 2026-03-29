package com.tutoring.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final FileUploadProperties fileUploadProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = fileUploadProperties.getUploadDir();
        
        String absolutePath = Paths.get(uploadDir).toAbsolutePath().normalize().toString();
        
        if (!absolutePath.endsWith("/") && !absolutePath.endsWith("\\")) {
            absolutePath = absolutePath + "/";
        }

        String resourceLocation = "file:" + absolutePath;

        registry.addResourceHandler("/uploads/**")
            .addResourceLocations(resourceLocation);

        registry.addResourceHandler("/doc.html")
            .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

}

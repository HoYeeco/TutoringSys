package com.tutoring;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableRetry
@MapperScan("com.tutoring.mapper")
public class TutoringBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TutoringBackendApplication.class, args);
    }

}

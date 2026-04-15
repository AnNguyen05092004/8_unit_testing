package com.doan2025.webtoeic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WebtoeicApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebtoeicApplication.class, args);
    }

}

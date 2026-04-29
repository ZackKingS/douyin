package com.douyin.backend;

import com.douyin.backend.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class DouyinBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DouyinBackendApplication.class, args);
    }
}

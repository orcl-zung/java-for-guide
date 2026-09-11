package com.activity.engine;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.activity.engine.infra.mapper")
public class ActivityEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActivityEngineApplication.class, args);
    }
}

package com.example.maimaibackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@MapperScan("com.example.maimaibackend.mapper")
@SpringBootApplication
public class MaimaiBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaimaiBackendApplication.class, args);
    }
}
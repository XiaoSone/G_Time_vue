package com.example.g_time;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.g_time", "com.example.g_time.config"})
public class GTimeApplication {

    public static void main(String[] args) {
        SpringApplication.run(GTimeApplication.class, args);
    }

}

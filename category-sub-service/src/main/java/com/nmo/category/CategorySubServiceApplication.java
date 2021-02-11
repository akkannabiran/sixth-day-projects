package com.sixthday.category;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Slf4j
@SpringBootApplication
@EnableCircuitBreaker
@EnableAspectJAutoProxy
public class CategorySubServiceApplication {

    protected CategorySubServiceApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.run(CategorySubServiceApplication.class, args);
    }
}
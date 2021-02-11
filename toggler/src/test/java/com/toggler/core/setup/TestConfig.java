package com.toggler.core.setup;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;


@SpringBootApplication
@ComponentScan({"com.toggler"})
public class TestConfig extends AsyncConfigurerSupport implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(TestConfig.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
    }
}

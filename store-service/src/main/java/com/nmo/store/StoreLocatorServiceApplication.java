package com.sixthday.store;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.strategy.HystrixPlugins;
import com.sixthday.store.filter.RedirectFilter;
import com.sixthday.store.hooks.RequestContextRegistratorCommandHook;

import lombok.extern.slf4j.Slf4j;

/**
 * This is the executable main class that invokes the Store service Application.
 */
@SpringBootApplication
@Slf4j
@EnableCircuitBreaker
@EnableCaching
public class StoreLocatorServiceApplication implements CommandLineRunner {

    public static void main(String... args) {
        SpringApplication.run(StoreLocatorServiceApplication.class, args);
        HystrixPlugins.getInstance().registerCommandExecutionHook(new RequestContextRegistratorCommandHook());
    }

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public RedirectFilter getRedirectFilter() {
        return new RedirectFilter();
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Store Locator application started.");
    }
}

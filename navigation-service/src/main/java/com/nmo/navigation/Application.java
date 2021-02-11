package com.sixthday.navigation;

import io.netty.util.NettyRuntime;
import java.util.concurrent.Executor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.api.config.NavigationServiceConfig.ThreadPoolConfig;
import com.sixthday.navigation.api.executors.ContextAwareAsyncTaskDecorator;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableCaching
@EnableCircuitBreaker
@ComponentScan(basePackages = {"com.sixthday.navigation", "com.sixthday.navigation.api"})
public class Application {
    private static final int DEFAULT_THREAD_POOL_CORE_SIZE = 100;

    protected Application() {
        NettyRuntime.availableProcessors();
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean(value = "requestContextAwareExecutor")
    public Executor getAsyncExecutor(NavigationServiceConfig navigationServiceConfig) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setTaskDecorator(new ContextAwareAsyncTaskDecorator());
        ThreadPoolConfig threadPoolConfig = navigationServiceConfig.getThreadPoolConfig();
        executor.setCorePoolSize(threadPoolConfig != null ? threadPoolConfig.getCoreSize() : DEFAULT_THREAD_POOL_CORE_SIZE);
        executor.initialize();
        return executor;
    }
    
    @Bean(value = "nonContextAwareExecutor")
    public Executor getNonContextAwareAsyncExecutor(NavigationServiceConfig navigationServiceConfig) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        ThreadPoolConfig threadPoolConfig = navigationServiceConfig.getThreadPoolConfig();
        executor.setCorePoolSize(threadPoolConfig != null ? threadPoolConfig.getCoreSize() : DEFAULT_THREAD_POOL_CORE_SIZE);
        executor.initialize();
        return executor;
    }
}

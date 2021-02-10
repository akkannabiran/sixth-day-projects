package com.sixthday.kafka.consumer.config;

import com.sixthday.kafka.app.factory.YamlPropertySourceFactory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Map;
import java.util.Properties;

@Getter
@Setter
@Configuration
@EnableConfigurationProperties
@PropertySource(value = {"classpath:consumer.yml", "classpath:consumer-${spring.profiles.active}.yml"}, factory = YamlPropertySourceFactory.class, ignoreResourceNotFound = true)
@ConfigurationProperties(prefix = "application")
public class ConsumerProperties {
    private Kafka kafka;

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setBeanName("threadPoolTaskScheduler");
        threadPoolTaskScheduler.setPoolSize(5);
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        return threadPoolTaskScheduler;
    }

    @Getter
    @Setter
    public static class Kafka {
        private String clusterApiKey;
        private String clusterApiSecret;
        private String serviceAccountId;
        private String monitoringClusterApiKey;
        private String monitoringClusterApiSecret;
        private String monitoringServiceAccountId;
        private Consumer consumer;
        private Producer producer;
        private Map<String, Listener> listeners;
    }

    @Getter
    @Setter
    public static class Consumer {
        private Properties properties;
    }

    @Getter
    @Setter
    public static class Producer {
        private Properties properties;
    }

    @Getter
    @Setter
    public static class Listener {
        private ListenerInfo consumer;
        private ListenerInfo retryProducer;
        private ListenerInfo dlProducer;
    }

    @Getter
    @Setter
    public static class ListenerInfo {
        private int commitRetries = 3;
        private boolean syncCommit = true;
        private long messagePollStatusDelay = 60;
        private String scheduleStart;
        private String topic;
        private String ackMode = "MANUAL_IMMEDIATE";
        private Properties properties;
    }
}

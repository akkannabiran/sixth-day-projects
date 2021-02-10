package com.sixthday.kafka.stream.config;

import com.sixthday.kafka.app.factory.YamlPropertySourceFactory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Map;
import java.util.Properties;

@Getter
@Setter
@Configuration
@EnableConfigurationProperties
@PropertySource(value = {"classpath:stream.yml", "classpath:stream-${spring.profiles.active}.yml"}, factory = YamlPropertySourceFactory.class, ignoreResourceNotFound = true)
@ConfigurationProperties(prefix = "application")
public class StreamProperties {
    private Kafka kafka;

    @Getter
    @Setter
    public static class Kafka {
        private String clusterApiKey;
        private String clusterApiSecret;
        private String serviceAccountId;
        private Stream stream;
        private Producer producer;
    }

    @Getter
    @Setter
    public static class Producer {
        private Properties properties;
    }

    @Getter
    @Setter
    public static class Stream {
        private Properties properties;
        private Map<String, StreamInfo> apps;
    }

    @Getter
    @Setter
    public static class StreamInfo {
        private String sourceTopic;
        private String targetTopic;
        private String dlTopic;
    }
}

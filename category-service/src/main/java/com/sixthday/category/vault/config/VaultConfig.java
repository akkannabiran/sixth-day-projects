package com.sixthday.category.vault.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "category-service")
@Getter
@Setter
public class VaultConfig {

    private Elasticsearch elasticsearch;
    private Elasticsearch6 elasticsearch6;

    @Getter
    @Setter
    public static class Elasticsearch {
        private String username;
        private String password;
    }

    @Getter
    @Setter
    public static class Elasticsearch6 {
        private String username;
        private String password;
    }
}

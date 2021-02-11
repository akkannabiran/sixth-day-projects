package com.sixthday.navigation.config;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.sixthday.navigation.api.config.NavigationServiceConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class S3Config {

    private NavigationServiceConfig navigationServiceConfig;

    @Autowired
    public S3Config(NavigationServiceConfig navigationServiceConfig) {
        this.navigationServiceConfig = navigationServiceConfig;
    }

    @Bean
    @SneakyThrows
    public AmazonS3Client amazonS3Client() {
        return (AmazonS3Client) AmazonS3ClientBuilder.standard()
                .withRegion(navigationServiceConfig.getStorage().getS3().getClientRegion())
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .build();
    }
}

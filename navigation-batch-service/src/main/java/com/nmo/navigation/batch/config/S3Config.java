package com.sixthday.navigation.batch.config;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import com.sixthday.navigation.config.NavigationBatchServiceConfig.Storage.S3;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

    private NavigationBatchServiceConfig navigationBatchServiceConfig;

    @Autowired
    public S3Config(NavigationBatchServiceConfig navigationBatchServiceConfig) {
        this.navigationBatchServiceConfig = navigationBatchServiceConfig;
    }

    @Bean
    @SneakyThrows
    public AmazonS3Client amazonS3Client() {
        S3 s3 = navigationBatchServiceConfig.getStorage().getS3();
        String clientRegion = s3.getClientRegion() == null ? Regions.US_WEST_2.getName() : s3.getClientRegion();

        return (AmazonS3Client) AmazonS3ClientBuilder.standard()
                .withRegion(clientRegion)
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .build();
    }
}
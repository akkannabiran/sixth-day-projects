package com.sixthday.navigation.integration.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.sixthday.logger.logging.LoggableEvent;
import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.config.Constants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Slf4j
@Component
public class AmazonS3ClientUtil {

    private NavigationServiceConfig navigationServiceConfig;
    private Environment environment;
    private AmazonS3 s3Client;

    @Autowired
    public AmazonS3ClientUtil(NavigationServiceConfig navigationServiceConfig,
                              AmazonS3 s3Client,
                              Environment environment) {
        this.navigationServiceConfig = navigationServiceConfig;
        this.s3Client = s3Client;
        this.environment = environment;
    }

    @SneakyThrows
    @LoggableEvent(eventType = Constants.API, action = Constants.AMAZON_S3_GET_SILO)
    public String getObject(String siloNavTreeId) {
        String envName = this.environment.getProperty("ENV_NAME");
        String bucketName = this.navigationServiceConfig.getStorage().getS3().getBucketName() == null ? "navigation-batch" : navigationServiceConfig.getStorage().getS3().getBucketName();

        String filePathAndName = envName + "/" + siloNavTreeId + "/" + "data.json";

        try (S3Object s3Object = this.s3Client.getObject(new GetObjectRequest(bucketName, filePathAndName))) {
            try (InputStream inputStream = s3Object.getObjectContent()) {
                return convertInputStream(inputStream);
            }
        }
    }

    @SneakyThrows
    private String convertInputStream(InputStream input) {
        return IOUtils.toString(input, "UTF-8");
    }
}

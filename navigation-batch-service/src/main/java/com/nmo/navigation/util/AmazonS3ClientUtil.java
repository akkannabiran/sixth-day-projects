package com.sixthday.navigation.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import com.sixthday.navigation.domain.SiloNavTree;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.Charset;

@Slf4j
@Component
public class AmazonS3ClientUtil {
    private static final String FILE_CONTENT_TYPE = "application/json";
    private static final String FILE_CONTENT_ENCODING = "UTF-8";

    private NavigationBatchServiceConfig navigationBatchServiceConfig;
    private AmazonS3 amazonS3;
    private Environment environment;

    @Autowired
    public AmazonS3ClientUtil(NavigationBatchServiceConfig navigationBatchServiceConfig, AmazonS3 amazonS3, Environment environment) {
        this.navigationBatchServiceConfig = navigationBatchServiceConfig;
        this.amazonS3 = amazonS3;
        this.environment = environment;
    }

    @SneakyThrows
    public void uploadObject(SiloNavTree siloNavTree) {
        String fileObjKeyName = "data.json";
        String envName = environment.getProperty("ENV_NAME");
        String bucketPath = navigationBatchServiceConfig.getStorage().getS3().getBucketName() + "/" + envName + "/" + siloNavTree.getId();

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byteArrayOutputStream.write(siloNavTree.getSiloData().getBytes(Charset.forName(AmazonS3ClientUtil.FILE_CONTENT_ENCODING)));

            try (InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray())) {

                log.debug("Writing to s3 @ " + bucketPath + "/" + fileObjKeyName);

                ObjectMetadata objectMetadata = new ObjectMetadata();
                objectMetadata.setContentType(AmazonS3ClientUtil.FILE_CONTENT_TYPE);
                objectMetadata.setContentEncoding(AmazonS3ClientUtil.FILE_CONTENT_ENCODING);
                objectMetadata.addUserMetadata("x-amz-meta-title", "Navigation Data for " + siloNavTree.getId());
                objectMetadata.setHeader("x-amz-acl", "bucket-owner-full-control");
                objectMetadata.setContentLength(byteArrayOutputStream.toByteArray().length);

                PutObjectRequest putObjectRequest = new PutObjectRequest(bucketPath, fileObjKeyName, inputStream, objectMetadata);
                amazonS3.putObject(putObjectRequest);
            }
        }
    }
}

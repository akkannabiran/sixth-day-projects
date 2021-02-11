package com.sixthday.navigation.repository;

import com.sixthday.navigation.integration.utils.AmazonS3ClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("S3SiloNavTreeReader")
public class S3SiloNavTreeReader implements SiloNavTreeReader {
    private AmazonS3ClientUtil siloNavTreeReader;

    @Autowired
    public S3SiloNavTreeReader(AmazonS3ClientUtil siloNavTreeReader) {
        this.siloNavTreeReader = siloNavTreeReader;
    }

    @Override
    public String loadSiloNavTree(final String navigationTreeId) {
        return siloNavTreeReader.getObject(navigationTreeId);
    }
}

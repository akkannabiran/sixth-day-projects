package com.sixthday.navigation.batch.io;

import com.sixthday.navigation.batch.vo.SiloNavTreeProcessorResponse;
import com.sixthday.navigation.domain.SiloNavTree;
import com.sixthday.navigation.exceptions.NavigationBatchServiceException;
import com.sixthday.navigation.util.AmazonS3ClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.sixthday.sixthdayLogging.OperationType.S3_UPDATE_SILO;
import static com.sixthday.sixthdayLogging.logOperation;

@Slf4j
@Component("S3SiloNavTreeWriter")
public class S3SiloNavTreeWriter implements SiloNavTreeWriter {

    private AmazonS3ClientUtil amazonS3ClientUtil;

    @Autowired
    public S3SiloNavTreeWriter(AmazonS3ClientUtil amazonS3ClientUtil) {
        this.amazonS3ClientUtil = amazonS3ClientUtil;
    }

    @Override
    public void write(SiloNavTreeProcessorResponse siloNavTreeProcessorResponse) {
        if (null == siloNavTreeProcessorResponse) {
            throw new NavigationBatchServiceException("navigationTreesData is empty or null");
        }

        String navTreeId = siloNavTreeProcessorResponse.getNavTreeId();
        logOperation(log, null, S3_UPDATE_SILO, navTreeId, () -> {
            amazonS3ClientUtil.uploadObject(new SiloNavTree(navTreeId, siloNavTreeProcessorResponse.getSilo()));
            return null;
        });
    }
}

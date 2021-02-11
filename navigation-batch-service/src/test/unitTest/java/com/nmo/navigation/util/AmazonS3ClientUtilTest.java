package com.sixthday.navigation.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.sixthday.navigation.batch.vo.SiloNavTreeProcessorResponse;
import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import com.sixthday.navigation.domain.SiloNavTree;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.env.Environment;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({DigestUtils.class})
public class AmazonS3ClientUtilTest {
    @InjectMocks
    AmazonS3ClientUtil mockAmazonS3ClientUtil;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    NavigationBatchServiceConfig navigationBatchServiceConfig;

    @Mock
    AmazonS3 amazonS3;

    @Mock
    Environment environment;

    @Mock
    S3Object s3Object;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        when(navigationBatchServiceConfig.getStorage().getS3().getBucketName()).thenReturn("navigation-bucket");
    }

    @Test
    public void testMockCreation() {
        assertNotNull(mockAmazonS3ClientUtil);
        assertNotNull(amazonS3);
    }

    @Test
    public void testUploadObject() {
        String countryCode = "US";
        String deviceType = "desktop";

        SiloNavTreeProcessorResponse navigationTreeData = new SiloNavTreeProcessorResponse(countryCode, deviceType,
                "{ test_json: true }", null);
        String siloNavTreeId = countryCode + "_" + deviceType;
        SiloNavTree siloNavTreeToSave = new SiloNavTree(siloNavTreeId, navigationTreeData.getSilo());

        PowerMockito.mockStatic(DigestUtils.class);
        BDDMockito.given(DigestUtils.md5Hex(any(String.class))).willReturn("randomhash");

        ObjectMetadata header = new ObjectMetadata();
        header.setContentMD5("randomhash");

        when(amazonS3.getObjectMetadata(any(String.class), any(String.class)))
                .thenAnswer((invocationOsixthdayck) -> header);

        mockAmazonS3ClientUtil.uploadObject(siloNavTreeToSave);

        verify(amazonS3).putObject(any(PutObjectRequest.class));
    }

    @Test(expected = NullPointerException.class)
    public void testClosesByteArrayOutputStreamConnection() {
        SiloNavTree siloNavTree = mock(SiloNavTree.class);
        when(siloNavTree.getId()).thenReturn("someId");
        when(siloNavTree.getSiloData()).thenReturn(null);
        mockAmazonS3ClientUtil.uploadObject(siloNavTree);
    }

    @Test(expected = Exception.class)
    public void testClosesInputStreamConnection() {
        SiloNavTree siloNavTree = mock(SiloNavTree.class);
        when(siloNavTree.getId()).thenReturn("someId");
        when(siloNavTree.getSiloData()).thenReturn("someSiloData");
        doThrow(Exception.class).when(amazonS3).putObject(any(PutObjectRequest.class));
        mockAmazonS3ClientUtil.uploadObject(siloNavTree);
    }
}

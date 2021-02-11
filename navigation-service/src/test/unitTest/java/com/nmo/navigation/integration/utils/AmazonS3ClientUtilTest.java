package com.sixthday.navigation.integration.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.sixthday.navigation.api.config.NavigationServiceConfig;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import java.io.*;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AmazonS3ClientUtilTest {
    @InjectMocks
    AmazonS3ClientUtil mockAmazonS3ClientUtil;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    NavigationServiceConfig navigationServiceConfig = new NavigationServiceConfig();

    @Mock
    AmazonS3 s3Client;

    @Mock
    Environment environment;

    @Mock
    S3Object mockS3Object;

    @Mock
    NavigationServiceConfig.Storage storage;

    @Mock
    NavigationServiceConfig.Storage.S3 s3;

    @Before
    public void setupMock() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        mockAmazonS3ClientUtil = null;
        s3Client = null;
        navigationServiceConfig = null;
    }

    @Test
    public void testMockCreation() {
        assertNotNull(navigationServiceConfig);
        assertNotNull(mockAmazonS3ClientUtil);
        assertNotNull(s3Client);
    }

    @Test
    public void testGetObject() {

        String countryCode = "US";
        String deviceType = "desktop";
        String siloNavTreeId = countryCode + "_" + deviceType;

        when(this.s3Client.getObject(any(GetObjectRequest.class))).thenAnswer((invocationOnMock) -> mockS3Object);

        this.navigationServiceConfig.getStorage().getS3().setBucketName("navigation-batch");
        this.navigationServiceConfig.getStorage().getS3().setClientRegion("us-west-2");

        ObjectMetadata mockObjectMetadata = mock(ObjectMetadata.class);
        when(mockS3Object.getObjectMetadata()).thenReturn(mockObjectMetadata);

        String initialString = "{ test_json: true }";
        InputStream inputStream = new ByteArrayInputStream(initialString.getBytes());
        S3ObjectInputStream s3ObjectInputStream = new S3ObjectInputStream(inputStream, null);
        when(mockS3Object.getObjectContent()).thenReturn(s3ObjectInputStream);

        String data = mockAmazonS3ClientUtil.getObject(siloNavTreeId);

        assertNotNull(data);
    }

    @Test
    public void testGetObjectNullBucket() throws IOException {

        String countryCode = "US";
        String deviceType = "desktop";
        String siloNavTreeId = countryCode + "_" + deviceType;

        when(this.s3Client.getObject(any(GetObjectRequest.class))).thenAnswer((invocationOnMock) -> mockS3Object);
        when(this.navigationServiceConfig.getStorage().getS3().getBucketName()).thenReturn(null);

        this.navigationServiceConfig.getStorage().getS3().setClientRegion("us-west-2");

        ObjectMetadata mockObjectMetadata = mock(ObjectMetadata.class);
        when(mockS3Object.getObjectMetadata()).thenReturn(mockObjectMetadata);

        String initialString = "{ test_json: true }";
        InputStream inputStream = new ByteArrayInputStream(initialString.getBytes());
        S3ObjectInputStream s3ObjectInputStream = new S3ObjectInputStream(inputStream, null);
        when(mockS3Object.getObjectContent()).thenReturn(s3ObjectInputStream);

        String data = mockAmazonS3ClientUtil.getObject(siloNavTreeId);

        assertNotNull(data);
    }

    @Test
    public void testGetObjectEmptyBucket() {

        String countryCode = "US";
        String deviceType = "desktop";
        String siloNavTreeId = countryCode + "_" + deviceType;

        when(this.s3Client.getObject(any(GetObjectRequest.class))).thenAnswer((invocationOnMock) -> mockS3Object);
        when(this.navigationServiceConfig.getStorage().getS3().getBucketName()).thenReturn("");

        this.navigationServiceConfig.getStorage().getS3().setClientRegion("us-west-2");

        ObjectMetadata mockObjectMetadata = mock(ObjectMetadata.class);
        when(mockS3Object.getObjectMetadata()).thenReturn(mockObjectMetadata);

        String initialString = "{ test_json: true }";
        InputStream inputStream = new ByteArrayInputStream(initialString.getBytes());
        S3ObjectInputStream s3ObjectInputStream = new S3ObjectInputStream(inputStream, null);
        when(mockS3Object.getObjectContent()).thenReturn(s3ObjectInputStream);

        String data = mockAmazonS3ClientUtil.getObject(siloNavTreeId);

        assertNotNull(data);
    }
}

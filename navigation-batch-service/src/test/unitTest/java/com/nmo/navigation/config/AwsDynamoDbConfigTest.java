package com.sixthday.navigation.config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.sixthday.navigation.exceptions.DynamoDBConfigurationException;

@RunWith(MockitoJUnitRunner.class)
public class AwsDynamoDbConfigTest {
  @Mock
  private AWSCredentials amazonAWSCredentials;
  private AwsDynamoDbConfig awsDynamoDbConfig = new AwsDynamoDbConfig();
  
  private static final String ENDPOINT = "endpoint";
  private static final String ACCESS_KEY = "access_key";
  private static final String SECRET_KEY = "secret_key";
  private static final String TABLE_NAME_PREFIX = "prefix";
  private static final long READ_CAPACITY_UNITS = 5;
  private static final long WRITE_CAPACITY_UNITS = 5;
  
  @Before
  public void init() {
    awsDynamoDbConfig.setEndpoint(ENDPOINT);
    awsDynamoDbConfig.setAccessKey(ACCESS_KEY);
    awsDynamoDbConfig.setSecretKey(SECRET_KEY);
    awsDynamoDbConfig.setRegion(Regions.DEFAULT_REGION.getName());
    awsDynamoDbConfig.setTableNamePrefix(TABLE_NAME_PREFIX);
    awsDynamoDbConfig.setReadCapacityUnits(READ_CAPACITY_UNITS);
    awsDynamoDbConfig.setWriteCapacityUnits(WRITE_CAPACITY_UNITS);
  }
  
  @Test
  public void testGetAmazonDynamoDB() {
    AmazonDynamoDB amazonDynamoDB = awsDynamoDbConfig.amazonDynamoDB(amazonAWSCredentials);
    assertNotNull(amazonDynamoDB);
  }
  
  @Test
  public void testGetDynamoDBMapper() {
    AmazonDynamoDB amazonDynamoDB = awsDynamoDbConfig.amazonDynamoDB(amazonAWSCredentials);
    DynamoDBMapper dynamoDBMapper = awsDynamoDbConfig.dynamoDbMapper(amazonDynamoDB);
    assertNotNull(dynamoDBMapper);
  }
  
  @Test
  public void testGetAmazonDynamoDBWithEmptyEndpoint() {
    awsDynamoDbConfig.setEndpoint(null);
    AmazonDynamoDB amazonDynamoDB = awsDynamoDbConfig.amazonDynamoDB(amazonAWSCredentials);
    assertNotNull(amazonDynamoDB);
  }
  
  @Test
  public void testGetAmazonDynamoDBWithEmptyCredentials() {
    awsDynamoDbConfig.setAccessKey(null);
    awsDynamoDbConfig.setSecretKey(null);
    AmazonDynamoDB amazonDynamoDB = awsDynamoDbConfig.amazonDynamoDB(amazonAWSCredentials);
    assertNotNull(amazonDynamoDB);
  }
  
  @Test(expected = DynamoDBConfigurationException.class)
  public void testGetAmazonDynamoDBWithEmptyTableNamePrefix() {
    awsDynamoDbConfig.setTableNamePrefix(null);
    awsDynamoDbConfig.amazonDynamoDB(amazonAWSCredentials);
  }
  @Test
  public void testGetAmazonDynamoDBWithNullCredentials() {
    awsDynamoDbConfig.setAccessKey(null);
    awsDynamoDbConfig.setSecretKey(null);
    AmazonDynamoDB amazonDynamoDB = awsDynamoDbConfig.amazonDynamoDB(amazonAWSCredentials);
    assertNotNull(amazonDynamoDB);
  }
  
  @Test
  public void testGetAmazonDynamoDBWithInvalidCredentials() {
    awsDynamoDbConfig.setAccessKey("SomeAccessKey");
    awsDynamoDbConfig.setSecretKey("");
    AmazonDynamoDB amazonDynamoDB = awsDynamoDbConfig.amazonDynamoDB(amazonAWSCredentials);
    assertNotNull(amazonDynamoDB);
  }
  
  @Test
  public void testGetAmazonDynamoDBWithEmptyRegion() {
    awsDynamoDbConfig.setRegion(null);
    AmazonDynamoDB amazonDynamoDB = awsDynamoDbConfig.amazonDynamoDB(amazonAWSCredentials);
    assertNotNull(amazonDynamoDB);
  }
  
  @Test
  public void testAmazonDynamoDBClientSetupForLocalInstace() {
    awsDynamoDbConfig.setAccessKey("dummy");
    awsDynamoDbConfig.setSecretKey("dummy");
    awsDynamoDbConfig.setEndpoint("SomeEndPoint");
    AmazonDynamoDB amazonDynamoDB = awsDynamoDbConfig.amazonDynamoDB(amazonAWSCredentials);
    
    assertThat(amazonDynamoDB, Matchers.instanceOf(AmazonDynamoDBClient.class));
  }
  
  @Test
  public void testAmazonDynamoDBClientSetupWhenOnlyEndpointIsConfigured() {
    awsDynamoDbConfig.setAccessKey(null);
    awsDynamoDbConfig.setSecretKey(null);
    awsDynamoDbConfig.setEndpoint("SomeEndPoint");
    AmazonDynamoDB amazonDynamoDB = awsDynamoDbConfig.amazonDynamoDB(amazonAWSCredentials);
    assertNotNull(amazonDynamoDB);
    assertThat(amazonDynamoDB, Matchers.instanceOf(AmazonDynamoDBClient.class));
  }
  
  @Test
  public void testAmazonDynamoDBClientSetupWhenOnlyAccessKeyIsConfigured() {
    awsDynamoDbConfig.setAccessKey("Dummy");
    awsDynamoDbConfig.setSecretKey(null);
    awsDynamoDbConfig.setEndpoint(null);
    AmazonDynamoDB amazonDynamoDB = awsDynamoDbConfig.amazonDynamoDB(amazonAWSCredentials);
    assertNotNull(amazonDynamoDB);
    assertThat(amazonDynamoDB, Matchers.instanceOf(AmazonDynamoDBClient.class));
  }
  
  @Test
  public void testAmazonDynamoDBClientSetupWhenOnlySecretKeyIsConfigured() {
    awsDynamoDbConfig.setAccessKey(null);
    awsDynamoDbConfig.setSecretKey("Dummy");
    awsDynamoDbConfig.setEndpoint(null);
    AmazonDynamoDB amazonDynamoDB = awsDynamoDbConfig.amazonDynamoDB(amazonAWSCredentials);
    assertNotNull(amazonDynamoDB);
    assertThat(amazonDynamoDB, Matchers.instanceOf(AmazonDynamoDBClient.class));
  }
  
  @Test
  public void testAmazonDynamoDBClientSetupWhenOnlyEndpointIsMissing() {
    awsDynamoDbConfig.setAccessKey("dummy");
    awsDynamoDbConfig.setSecretKey("dummy");
    awsDynamoDbConfig.setEndpoint(null);
    AmazonDynamoDB amazonDynamoDB = awsDynamoDbConfig.amazonDynamoDB(amazonAWSCredentials);
    assertNotNull(amazonDynamoDB);
    assertThat(amazonDynamoDB, Matchers.instanceOf(AmazonDynamoDBClient.class));
  }
  
  @Test
  public void testAmazonDynamoDBClientSetupWhenOnlyAccessKeyIsMissing() {
    awsDynamoDbConfig.setAccessKey(null);
    awsDynamoDbConfig.setSecretKey("dummy");
    awsDynamoDbConfig.setEndpoint("dummy");
    AmazonDynamoDB amazonDynamoDB = awsDynamoDbConfig.amazonDynamoDB(amazonAWSCredentials);
    assertNotNull(amazonDynamoDB);
    assertThat(amazonDynamoDB, Matchers.instanceOf(AmazonDynamoDBClient.class));
  }
  
  @Test
  public void testAmazonDynamoDBClientSetupWhenOnlySecretKeyIsMissing() {
    awsDynamoDbConfig.setAccessKey("dummy");
    awsDynamoDbConfig.setSecretKey(null);
    awsDynamoDbConfig.setEndpoint("dummy");
    AmazonDynamoDB amazonDynamoDB = awsDynamoDbConfig.amazonDynamoDB(amazonAWSCredentials);
    assertNotNull(amazonDynamoDB);
    assertThat(amazonDynamoDB, Matchers.instanceOf(AmazonDynamoDBClient.class));
  }
  
  @Test
  public void testAmazonDynamoDBClientSetupForEC2Instace() {
    awsDynamoDbConfig.setAccessKey(null);
    awsDynamoDbConfig.setSecretKey(null);
    awsDynamoDbConfig.setEndpoint(null);
    AmazonDynamoDB amazonDynamoDB = awsDynamoDbConfig.amazonDynamoDB(amazonAWSCredentials);
    assertNotNull(amazonDynamoDB);
    assertThat(amazonDynamoDB, Matchers.instanceOf(AmazonDynamoDBClient.class));
  }
  
  @Test
  public void testAmazonAWSCredentialsForLocalInstanceWhenAccessAndSecretKeysAreSet() {
    awsDynamoDbConfig.setAccessKey("DummyAccessKey");
    awsDynamoDbConfig.setSecretKey("DummySecretKey");
    AWSCredentials creds =  awsDynamoDbConfig.amazonAWSCredentials();
    assertNotNull(creds);
    assertThat(creds, Matchers.instanceOf(BasicAWSCredentials.class));
  }
  
  @Test
  public void testAmazonAWSCredentialsForWhenOnlyAccessKeySet() {
    awsDynamoDbConfig.setAccessKey("DummyAccessKey");
    awsDynamoDbConfig.setSecretKey(null);
    AWSCredentials creds =  awsDynamoDbConfig.amazonAWSCredentials();
    assertNotNull(creds);
    assertThat(creds, Matchers.instanceOf(AWSCredentials.class));
  }
  
  @Test
  public void testAmazonAWSCredentialsForWhenOnlySecretKeySet() {
    awsDynamoDbConfig.setAccessKey(null);
    awsDynamoDbConfig.setSecretKey("DummySecretKey");
    AWSCredentials creds =  awsDynamoDbConfig.amazonAWSCredentials();
    assertNotNull(creds);
    assertThat(creds, Matchers.instanceOf(AWSCredentials.class));
  }
}

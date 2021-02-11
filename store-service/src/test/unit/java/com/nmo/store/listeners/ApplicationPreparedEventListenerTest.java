package com.sixthday.store.listeners;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.ByteArrayOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.boot.context.event.ApplicationPreparedEvent;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.InternalServerErrorException;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.LimitExceededException;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.sixthday.store.LogCapture;
import com.sixthday.store.config.AwsDynamoDbConfig;
import com.sixthday.store.models.StoreInventoryBySKUDocument;

import lombok.SneakyThrows;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ApplicationPreparedEventListener.class, TableUtils.class})
@PowerMockIgnore("javax.management.*")
public class ApplicationPreparedEventListenerTest {
  
  private ByteArrayOutputStream loggingOutput;
  
  @Mock
  private ApplicationPreparedEvent preparedEvent;
  
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private AwsDynamoDbConfig awsDynamoDbConfig;
  
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private AmazonDynamoDB amazonDynamoDB;
  
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private DynamoDBMapperConfig dynamoDBMapperConfig;
  
  @InjectMocks
  private ApplicationPreparedEventListener applicationPreparedEventListener;
  
  @BeforeClass
  public static void setLoggerContextSelector() {
    System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.selector.BasicContextSelector");
  }
  
  @Before
  public void setUp() throws Exception {
    loggingOutput = LogCapture.captureLogOutput(ApplicationPreparedEventListener.class);
    mockStatic(TableUtils.class);
    doNothing().when(TableUtils.class, "waitUntilActive", eq(amazonDynamoDB), anyString());
    when(dynamoDBMapperConfig.getTableNameResolver().getTableName(eq(StoreInventoryBySKUDocument.class), eq(dynamoDBMapperConfig))).thenReturn("test_store_inventory_by_sku");
    when(awsDynamoDbConfig.getReadCapacityUnits()).thenReturn(5L);
    when(awsDynamoDbConfig.getWriteCapacityUnits()).thenReturn(5L);
  }
  
  @After
  public void tearDown() {
    System.out.println(new String(loggingOutput.toByteArray()));
    LogCapture.stopLogCapture(ApplicationPreparedEventListener.class, loggingOutput);
  }
  
  private CreateTableRequest prepareCreateTableRequest() {
    CreateTableRequest createTableRequest = new CreateTableRequest().withTableName("test_store_inventory_by_sku");
    createTableRequest.withKeySchema(new KeySchemaElement().withAttributeName("sku_id").withKeyType(KeyType.HASH));
    createTableRequest.withAttributeDefinitions(new AttributeDefinition().withAttributeName("sku_id").withAttributeType(ScalarAttributeType.S));
    createTableRequest.setProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(5L).withWriteCapacityUnits(5L));
    return createTableRequest;
  }
  
  @Test
  public void shouldCreateTableIfNotExists() {
    
    applicationPreparedEventListener.onApplicationEvent(preparedEvent);
    
    ArgumentCaptor<CreateTableRequest> createRequestCaptor = ArgumentCaptor.forClass(CreateTableRequest.class); 
    verifyStatic(times(1));
    TableUtils.createTableIfNotExists(eq(amazonDynamoDB), createRequestCaptor.capture());
    CreateTableRequest actualCreateTableRequest = createRequestCaptor.getValue();
    CreateTableRequest expectedCreateTableRequest = prepareCreateTableRequest();
    assertThat("Create table request should match expected", actualCreateTableRequest, equalTo(expectedCreateTableRequest));
  }
  
  
  
  @Test(expected = InternalServerErrorException.class)
  public void shouldThrowInternalServerErrorExceptionWhenCreateTableThrowsInternalServerErrorException() {
    mockStatic(TableUtils.class);
    when(TableUtils.createTableIfNotExists(eq(amazonDynamoDB), any(CreateTableRequest.class))).thenThrow(new InternalServerErrorException("Internal server error from test case"));
    applicationPreparedEventListener.onApplicationEvent(preparedEvent);
  }
  
  @Test(expected = LimitExceededException.class)
  public void shouldThrowLimitExceededExceptionWhenCreateTableThrowsLimitExceededException() {
    when(TableUtils.createTableIfNotExists(eq(amazonDynamoDB), any(CreateTableRequest.class))).thenThrow(new LimitExceededException("LimitExceededException from test case"));
    applicationPreparedEventListener.onApplicationEvent(preparedEvent);
  }
  
  @Test
  @SneakyThrows
  public void shouldNotCreateTableIfExists() {
    when(TableUtils.createTableIfNotExists(eq(amazonDynamoDB), any(CreateTableRequest.class))).thenReturn(false);
    applicationPreparedEventListener.onApplicationEvent(preparedEvent);
    verifyStatic(never());
    TableUtils.waitUntilActive(any(), any());
  }
  
}

package com.sixthday.store.handlers;

import static com.sixthday.store.SplunkFieldsAssertionUtil.verifySplunkFieldsOnLog;
import static com.sixthday.store.models.storeinventoryindex.SkuStoresInventoryMessage.EventType.SKU_STORES_INVENTORY_UPDATED;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.sixthday.store.models.storeinventoryindex.SkuStoresInventoryMessage;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;

import com.sixthday.store.LogCapture;
import com.sixthday.store.cloud.stream.StoreInventoryBySKUAnnouncer;
import com.sixthday.store.config.Constants.Logging;
import com.sixthday.store.data.SkuStoresInventoryMessageBuilder;
import com.sixthday.store.services.StoreSkuInventorySyncService;
import com.sixthday.store.util.MDCUtils;

import lombok.SneakyThrows;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MDCUtils.class)
@PowerMockIgnore("javax.management.*")
public class SkuStoresInventoryMessageHandlerTest {
  
  @Mock
  private StoreSkuInventorySyncService storeSkuInventorySyncService;
  
  @Mock
  private StoreInventoryBySKUAnnouncer announcer;
  
  @InjectMocks
  private SkuStoresInventoryMessageHandler skuStoresInventoryMessageHandler;
  
  private ByteArrayOutputStream loggingOutput;
  
  @BeforeClass
  public static void setLoggerContextSelector() {
    System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.selector.BasicContextSelector");
  }
  
  @Before
  @SneakyThrows
  public void setUp() {
    loggingOutput = LogCapture.captureLogOutput(SkuStoresInventoryMessageHandler.class);
    System.setOut(new PrintStream(loggingOutput));
    FieldUtils.writeField(skuStoresInventoryMessageHandler, "contentSyncSource", "NameOfRabbitMQQueueNameGoesHere", true);
    FieldUtils.writeField(skuStoresInventoryMessageHandler, "contentSyncDestination", "NameOfDynamoDBTblGoesHere", true);
  }
  
  @After
  public void tearDown() {
    LogCapture.stopLogCapture(SkuStoresInventoryMessageHandler.class, loggingOutput);
  }
  
  private Message buildMessage(String messageBody) {
    return MessageBuilder.withBody(messageBody.getBytes()).setType(SKU_STORES_INVENTORY_UPDATED.name()).build();
  }
  
  @Test
  public void shouldCallServiceWithDeserializedMessage() {
    SkuStoresInventoryMessageBuilder builder = new SkuStoresInventoryMessageBuilder();
    String msg = builder.toJson();
    SkuStoresInventoryMessage invMsg = builder.build();
    skuStoresInventoryMessageHandler.handle(buildMessage(msg));
    verify(announcer).announceStoreSkuInventoryForProductMessage(invMsg);
    
    ArgumentCaptor<SkuStoresInventoryMessage> captor = ArgumentCaptor.forClass(SkuStoresInventoryMessage.class);
    verify(storeSkuInventorySyncService).updateStoreSkuInventory(captor.capture());
    assertThat("Acutal message object should match expected message object", captor.getValue(), equalTo(builder.build()));
  }
  
  @Test
  public void shouldCallMDCUtilsToSetValuesOnStoreSkuInventoryUpdatedMessageWhenMessageHasAllValues() {
    String msg = new SkuStoresInventoryMessageBuilder().toJson();
    PowerMockito.mockStatic(MDCUtils.class);
    skuStoresInventoryMessageHandler.handle(buildMessage(msg));
    verifyStatic(times(1));
    MDCUtils.setMDC("PRODUCT-ID", "SKU_STORES_INVENTORY_UPDATED", "BATCH-ID", "SomeOriginTimestamp", "NameOfRabbitMQQueueNameGoesHere", "NameOfDynamoDBTblGoesHere", Logging.CONTENT_SYNC_AWS_RESOURCE);
  }
  
  @Test
  public void shouldCallMDCUtilsWithNullContextIdAndOriginTimestampOnStoreSkuInventoryUpdatedMessageWhenMessageDoesntHaveValues() {
    String msg = new SkuStoresInventoryMessageBuilder().withBatchId(null).withOriginTimestampInfo(null).toJson();
    PowerMockito.mockStatic(MDCUtils.class);
    skuStoresInventoryMessageHandler.handle(buildMessage(msg));
    verifyStatic(times(1));
    MDCUtils.setMDC("PRODUCT-ID", "SKU_STORES_INVENTORY_UPDATED", null, null, "NameOfRabbitMQQueueNameGoesHere", "NameOfDynamoDBTblGoesHere", Logging.CONTENT_SYNC_AWS_RESOURCE);
  }
  
  @Test
  public void shouldLogContentSyncDashboardSuccessEventWhenMessageHasAllValues() {
    String msg = new SkuStoresInventoryMessageBuilder().toJson();
    
    skuStoresInventoryMessageHandler.handle(buildMessage(msg));
    
    String actualLog = new String(loggingOutput.toByteArray());
    String[] expected = {"SkuStoresMsgReceiverUpdate=\"SkuStoresMsgReceived\"," , "Status=\"Success\"," , "MessageId=\"PRODUCT-ID\"," , "MessageType=\"SKU_STORES_INVENTORY_UPDATED\"," ,
        "ContextId=\"BATCH-ID\"," , "OriginTimestamp=\"SomeOriginTimestamp\"," , "DurationInMs=\"" , "sixthdaySRC=\"NameOfRabbitMQQueueNameGoesHere\"" , "sixthdayDEST=\"NameOfDynamoDBTblGoesHere\"" ,
        "sixthdayRESOURCE=\"DynamoDB\""};
    verifySplunkFieldsOnLog(actualLog, expected);
  }
  
  @Test
  public void shouldLogContentSyncDashboardFailedEventWhenMessageHasAllValues() {
    String msg = new SkuStoresInventoryMessageBuilder().toString();
    
    skuStoresInventoryMessageHandler.handle(buildMessage(msg));
    
    String actualLog = new String(loggingOutput.toByteArray());
    String[] expected = {"SkuStoresMsgReceiverUpdate=\"SkuStoresMsgReceived\"," , "Status=\"Failed\"," , "MessageId=\"-\"," , "MessageType=\"DeserializationFailed\"," , "ContextId=\"-\"," , "OriginTimestamp=\"NA\"," ,
        "DurationInMs=\"" , "sixthdaySRC=\"NameOfRabbitMQQueueNameGoesHere\"" , "sixthdayDEST=\"NameOfDynamoDBTblGoesHere\"" , "sixthdayRESOURCE=\"DynamoDB\""};
    verifySplunkFieldsOnLog(actualLog, expected);
  }
}

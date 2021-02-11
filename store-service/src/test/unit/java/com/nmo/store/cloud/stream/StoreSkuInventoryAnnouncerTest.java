package com.sixthday.store.cloud.stream;

import static com.sixthday.store.SplunkFieldsAssertionUtil.verifySplunkFieldsOnLog;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.Message;

import com.sixthday.store.LogCapture;
import com.sixthday.store.data.StoreSkuInventoryAnnoucementBuilder;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryAnnouncement;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryMessage;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryMessage.EventType;

import lombok.SneakyThrows;

@RunWith(MockitoJUnitRunner.class)
public class StoreSkuInventoryAnnouncerTest {
  
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Source messageSource;
  
  @Captor
  private ArgumentCaptor<Message<?>> messageCaptor;
  
  @InjectMocks
  private StoreSkuInventoryAnnouncer inventoryAnnouncer;
  private ByteArrayOutputStream loggingOutput;
  @BeforeClass
  public static void setLoggerContextSelector() {
    System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.selector.BasicContextSelector");
  }
  
  @Before
  @SneakyThrows
  public void setUp() {
    loggingOutput = LogCapture.captureLogOutput(StoreSkuInventoryAnnouncer.class);
    System.setOut(new PrintStream(loggingOutput));
    FieldUtils.writeField(inventoryAnnouncer, "destination", "NameOfRabbitMQQueueNameGoesHere", true);
  }
  
  @After
  public void tearDown() {
    LogCapture.stopLogCapture(StoreSkuInventoryAnnouncer.class, loggingOutput);
  }
  @Test
  public void shouldReturnStoreSkuInventoryAnnoucementWhenAnnounceMethodIsInvoked() {
    
    StoreSkuInventoryMessage skuInventoryMsg = new StoreSkuInventoryMessage();
    skuInventoryMsg.setId("Id");
    skuInventoryMsg.setSkuId("SKU_ID");
    skuInventoryMsg.setProductIds(Arrays.asList("PRODUCT_ID1"));
    skuInventoryMsg.setStoreId("STORE_ID");
    skuInventoryMsg.setStoreNumber("STORE_NUMBER");
    skuInventoryMsg.setEventType(EventType.STORE_SKU_INVENTORY_UPSERT);
    skuInventoryMsg.setInventoryLevelCode("INVENTORY_LEVEL_CODE");
    skuInventoryMsg.setQuantity(1);
    skuInventoryMsg.setBatchId("SomeContextId");
    String originTimestamp = LocalDateTime.now().toString();
    skuInventoryMsg.setOriginTimestampInfo(Collections.singletonMap(EventType.STORE_SKU_INVENTORY_UPSERT.name(), originTimestamp));
    skuInventoryMsg.setDataPoints(Collections.singletonMap("S", originTimestamp));
    
    StoreSkuInventoryAnnouncement actual = inventoryAnnouncer.announceStoreSkuInventoryUpdate(skuInventoryMsg, true);
    
    verify(messageSource.output()).send(messageCaptor.capture());
    Message<?> sentMessage = messageCaptor.getValue();
    assertThat("Announcement is sent", sentMessage, not(nullValue()));
    assertThat("Announcement payload is not null", sentMessage.getPayload(), not(nullValue()));
    StoreSkuInventoryAnnouncement expected = new StoreSkuInventoryAnnoucementBuilder().withBatchId("SomeContextId")
                                                    .withRemoved(true).withOriginTimestampInfo(Collections.singletonMap(EventType.STORE_SKU_INVENTORY_UPSERT.name(), originTimestamp))
                                                    .withDataPoints(Collections.singletonMap("S", originTimestamp)).build();
    assertThat("Announcement sent should contain valid message", sentMessage.getPayload(), equalTo((expected)));
    
    assertThat("Actual Announcement returned should match expected ", actual, equalTo((expected)));
    
  }
  
  @Test
  public void shouldLogContentSyncDashboardSuccessEventWhenMessagePublishedSuccessfully() {
    StoreSkuInventoryMessage skuInventoryMsg = new StoreSkuInventoryMessage();
    skuInventoryMsg.setId("Id");
    skuInventoryMsg.setSkuId("SKU_ID");
    skuInventoryMsg.setProductIds(Arrays.asList("PRODUCT_ID1"));
    skuInventoryMsg.setStoreId("STORE_ID");
    skuInventoryMsg.setStoreNumber("STORE_NUMBER");
    skuInventoryMsg.setEventType(EventType.STORE_SKU_INVENTORY_UPSERT);
    skuInventoryMsg.setInventoryLevelCode("INVENTORY_LEVEL_CODE");
    skuInventoryMsg.setQuantity(1);
    skuInventoryMsg.setBatchId("SomeContextId");
    String originTimestamp = LocalDateTime.now().toString();
    skuInventoryMsg.setOriginTimestampInfo(Collections.singletonMap(EventType.STORE_SKU_INVENTORY_UPSERT.name(), originTimestamp));
    skuInventoryMsg.setDataPoints(Collections.singletonMap("S", originTimestamp));
    
    when(messageSource.output().send(any())).thenReturn(true);
    
    inventoryAnnouncer.announceStoreSkuInventoryUpdate(skuInventoryMsg, true);
    
    String actualLog = new String(loggingOutput.toByteArray());
    String[] expected =
            {"StoreSkuInventoryAnnouncerUpdate=\"InventoryRemoved\"," , "Status=\"Success\"," , "MessageId=\"Id\"," , "MessageType=\"STORE_SKU_INVENTORY_UPSERT\"," , "ContextId=\"SomeContextId\"," ,
                "OriginTimestamp=\""+originTimestamp+"\"," , "DurationInMs=\"" , "sixthdaySRC=\"APP:store-service\"" , "sixthdayDEST=\"NameOfRabbitMQQueueNameGoesHere\"" , "sixthdayRESOURCE=\"-\""};
    verifySplunkFieldsOnLog(actualLog, expected);
  }
  
  @Test
  public void shouldLogContentSyncDashboardFailureEventWhenPublishFailed() {
    
    when(messageSource.output().send(any())).thenReturn(false);
    StoreSkuInventoryMessage skuInventoryMsg = new StoreSkuInventoryMessage();
    skuInventoryMsg.setId("Id");
    skuInventoryMsg.setSkuId("SKU_ID");
    skuInventoryMsg.setProductIds(Arrays.asList("PRODUCT_ID1"));
    skuInventoryMsg.setStoreId("STORE_ID");
    skuInventoryMsg.setStoreNumber("STORE_NUMBER");
    skuInventoryMsg.setEventType(EventType.STORE_SKU_INVENTORY_UPSERT);
    skuInventoryMsg.setInventoryLevelCode("INVENTORY_LEVEL_CODE");
    skuInventoryMsg.setQuantity(1);
    skuInventoryMsg.setBatchId("SomeContextId");
    String originTimestamp = LocalDateTime.now().toString();
    skuInventoryMsg.setOriginTimestampInfo(Collections.singletonMap(EventType.STORE_SKU_INVENTORY_UPSERT.name(), originTimestamp));
    skuInventoryMsg.setDataPoints(Collections.singletonMap("S", originTimestamp));
    
    inventoryAnnouncer.announceStoreSkuInventoryUpdate(skuInventoryMsg, true);
    
    String actualLog = new String(loggingOutput.toByteArray());
    String[] expected =
            {"StoreSkuInventoryAnnouncerUpdate=\"InventoryRemoved\"," , "Status=\"Failed\"," , "MessageId=\"Id\"," , "MessageType=\"STORE_SKU_INVENTORY_UPSERT\"," , "ContextId=\"SomeContextId\"," ,
                "OriginTimestamp=\""+originTimestamp+"\"," , "DurationInMs=\"" , "sixthdaySRC=\"APP:store-service\"" , "sixthdayDEST=\"NameOfRabbitMQQueueNameGoesHere\"" , "sixthdayRESOURCE=\"-\""};
    verifySplunkFieldsOnLog(actualLog, expected);
  }
  
  @Test(expected = Exception.class)
  public void shouldThrowExceptionWhenStoreSkuInventoryDocumentIsNull() {
    inventoryAnnouncer.announceStoreSkuInventoryUpdate(null, false);
  }
  
}

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

import com.sixthday.store.models.storeinventoryindex.SkuStoresInventoryMessage;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.messaging.Message;

import com.sixthday.store.LogCapture;
import com.sixthday.store.config.StoreInventoryBySKUSource;
import com.sixthday.store.data.SkuStoresInventoryMessageBuilder;

import lombok.SneakyThrows;

@RunWith(MockitoJUnitRunner.class)
public class StoreInventoryBySKUAnnouncerTest {
  
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private StoreInventoryBySKUSource messageSource;
  
  @InjectMocks
  private StoreInventoryBySKUAnnouncer storeInventoryBySKUAnnouncer;
  private ByteArrayOutputStream loggingOutput;
  
  @BeforeClass
  public static void setLoggerContextSelector() {
    System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.selector.BasicContextSelector");
  }
  
  @Before
  @SneakyThrows
  public void setUp() {
    loggingOutput = LogCapture.captureLogOutput(StoreInventoryBySKUAnnouncer.class);
    System.setOut(new PrintStream(loggingOutput));
    FieldUtils.writeField(storeInventoryBySKUAnnouncer, "contentSyncDestination", "NameOfRabbitMQQueueNameGoesHere", true);
  }
  
  @After
  public void tearDown() {
    LogCapture.stopLogCapture(StoreInventoryBySKUAnnouncer.class, loggingOutput);
  }
  
  @Test
  public void shouldReturnSentMessageWhenAnnounceMethodIsInvoked() {
    SkuStoresInventoryMessage receivedMessage = new SkuStoresInventoryMessageBuilder().build();
    ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
    SkuStoresInventoryMessage actual = storeInventoryBySKUAnnouncer.announceStoreSkuInventoryForProductMessage(receivedMessage);
    verify(messageSource.storeInventoryBySKUChannel()).send(messageCaptor.capture());
    
    Message<?> sentMessage = messageCaptor.getValue();
    
    assertThat("Announcement is sent", sentMessage, not(nullValue()));
    assertThat("Announcement payload is not null", sentMessage.getPayload(), not(nullValue()));
    
    assertThat("Published message should be same as received message", receivedMessage, equalTo(sentMessage.getPayload()));
    assertThat("Feedback message should be same as received message", actual, equalTo(receivedMessage));
  }
  
  @Test
  public void shouldLogContentSyncDashboardSuccessEventWhenMessagePublishedSuccessfully() {
    SkuStoresInventoryMessage message = new SkuStoresInventoryMessageBuilder().build();
    when(messageSource.storeInventoryBySKUChannel().send(any())).thenReturn(true);
    storeInventoryBySKUAnnouncer.announceStoreSkuInventoryForProductMessage(message);
    
    String actualLog = new String(loggingOutput.toByteArray());
    String[] expected =
            {"SkuStoresMsgAnnouncerUpdate=\"SkuStoresMsgPublished\"," , "Status=\"Success\"," , "MessageId=\"PRODUCT-ID\"," , "MessageType=\"SKU_STORES_INVENTORY_UPDATED\"," , "ContextId=\"BATCH-ID\"," ,
                "OriginTimestamp=\"SomeOriginTimestamp\"," , "DurationInMs=\"" , "sixthdaySRC=\"APP:store-service\"" , "sixthdayDEST=\"NameOfRabbitMQQueueNameGoesHere\"" , "sixthdayRESOURCE=\"-\""};
    verifySplunkFieldsOnLog(actualLog, expected);
  }
  
  @Test
  public void shouldLogContentSyncDashboardFailureEventWhenPublishFailed() {
    when(messageSource.storeInventoryBySKUChannel().send(any())).thenReturn(false);
    SkuStoresInventoryMessage message = new SkuStoresInventoryMessageBuilder().build();
    storeInventoryBySKUAnnouncer.announceStoreSkuInventoryForProductMessage(message);
    
    String actualLog = new String(loggingOutput.toByteArray());
    String[] expected =
            {"SkuStoresMsgAnnouncerUpdate=\"SkuStoresMsgPublished\"," , "Status=\"Failed\"," , "MessageId=\"PRODUCT-ID\"," , "MessageType=\"SKU_STORES_INVENTORY_UPDATED\"," , "ContextId=\"BATCH-ID\"," ,
                "OriginTimestamp=\"SomeOriginTimestamp\"," , "DurationInMs=\"" , "sixthdaySRC=\"APP:store-service\"" , "sixthdayDEST=\"NameOfRabbitMQQueueNameGoesHere\"" , "sixthdayRESOURCE=\"-\""};
    verifySplunkFieldsOnLog(actualLog, expected);
  }
  
  @Test(expected = Exception.class)
  public void shouldThrowExceptionWhenStoreSkuInventoryDocumentIsNull() {
    storeInventoryBySKUAnnouncer.announceStoreSkuInventoryForProductMessage(null);
  }
}

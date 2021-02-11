package com.sixthday.store;

import static com.sixthday.store.SplunkFieldsAssertionUtil.verifySplunkFieldsOnLog;
import static com.sixthday.store.models.storeindex.StoreMessage.EventType.STORE_UPSERT;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.amqp.core.Message;

import com.sixthday.store.config.Constants.Logging;
import com.sixthday.store.models.storeindex.StoreDocument;
import com.sixthday.store.models.storeindex.StoreMessage;
import com.sixthday.store.services.StoreSyncService;
import com.sixthday.store.util.MDCUtils;
import com.sixthday.store.util.sixthdayMDCAdapter;

import lombok.SneakyThrows;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value=MDCUtils.class)
@PowerMockIgnore("javax.management.*")
public class StoreMessageReceiverTest {

    @Mock
    private StoreSyncService storeSyncService;

    @Mock
    private sixthdayMDCAdapter mdc;
    
    @Mock
    private Message message;
    
    @InjectMocks
    private StoreMessageReceiver storeMessageReceiver;
    private ByteArrayOutputStream loggingOutput;
    
    @BeforeClass
    public static void setLoggerContextSelector(){
      System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.selector.BasicContextSelector");
    }
    @Before
    @SneakyThrows
    public void setUp() {
      loggingOutput = LogCapture.captureLogOutput(StoreMessageReceiver.class);
      System.setOut(new PrintStream(loggingOutput));
      FieldUtils.writeField(storeMessageReceiver, "contentSyncSource", "StoreQueue", true);
      FieldUtils.writeField(storeMessageReceiver, "contentSyncDestination", "StoreIndex", true);
      
    }
    @After
    public void tearDown(){
      LogCapture.stopLogCapture(StoreMessageReceiver.class, loggingOutput);
    }
    @Test
    public void shouldListenToQueueAndUpdateElasticSearch() throws Exception {
        String messageBody =
                "       {"+
                "			\"eventType\": \"STORE_UPSERT\"," +
                "\"batchId\":\"Sched-2018-04-03T16:20:32.025\","+
                "\"originTimestampInfo\":{\"STORE_UPSERT\":\"2018-04-03T16:20:32.215\"},"+
				"			\"id\": \"106/BL\"," +
				"			\"storeNumber\": \"106\"," +
				"			\"name\": \"Bellevue\"," +
				"			\"addressLine1\": \"11111 NE 8th Street\"," +
				"			\"addressLine2\": \"abc\"," +
				"			\"city\": \"Bellevue\"," +
				"			\"state\": \"WA\"," +
				"			\"zipCode\": \"98004\"," +
				"			\"phoneNumber\": \"425-452-3300\"," +
				"			\"storeHours\": \"Mon. 10:00AM - 8:00PM,Tue. 10:00AM - 8:00PM,Wed. 10:00AM - 8:00PM,Thu. 10:00AM - 8:00PM,Fri. 10:00AM - 8:00PM,Sat. 10:00AM - 8:00PM,Sun. 12:00PM - 6:00PM\"," +
				"			\"storeDescription\": \"<br>Sixthday is a renowned specialty store dedicated to merchandise leadership and superior customer service. We will offer the finest fashion and quality products in a welcoming environment. \"" +
				"	    }\"";
        
        when(message.getBody()).thenReturn(messageBody.getBytes());
        
        StoreMessage expectedStoreMessage = new StoreMessage("106/BL");
        expectedStoreMessage.setStoreNumber("106");
        expectedStoreMessage.setStoreName("Bellevue");
        expectedStoreMessage.setAddressLine1("11111 NE 8th Street");
        expectedStoreMessage.setCity("Bellevue");
        expectedStoreMessage.setState("WA");
        expectedStoreMessage.setZipCode("98004");
        expectedStoreMessage.setPhoneNumber("425-452-3300");
        expectedStoreMessage.setStoreHours("Mon. 10:00AM - 9:00PM,Tue. 10:00AM - 9:00PM," +
        						   "Wed. 10:00AM - 9:00PM,Thu. 10:00AM - 9:00PM," +
        						   "Fri. 10:00AM - 9:00PM,Sat. 10:00AM - 9:00PM,Sun. 12:00PM - 6:00PM"	);
        expectedStoreMessage.setStoreDescription("<br>Sixthday is a renowned specialty store dedicated" +
        						   				"to merchandise leadership and superior customer service." +
        						   				"We will offer the finest fashion and quality products in a welcoming environment. ");
        expectedStoreMessage.setEventType(STORE_UPSERT);
        
        storeMessageReceiver.onMessage(message, null);
        
        String actualLog = new String(loggingOutput.toByteArray());
        String[] expected = {"StoreMessageReceiverUpdate=\"STORE_MESSAGE_RECEIVED\",",
            "Status=\"Success\",",
            "MessageId=\"106/BL\",",
            "MessageType=\"STORE_UPSERT\",",
            "ContextId=\"Sched-2018-04-03T16:20:32.025\",",
            "OriginTimestamp=\"2018-04-03T16:20:32.215\",",
            "DurationInMs=\"",
            "sixthdaySRC=\"StoreQueue\"","sixthdayDEST=\"StoreIndex\"","sixthdayRESOURCE=\"ElasticSearch\""};
        verifySplunkFieldsOnLog(actualLog, expected);
    }
    
    @Test
    public void shouldPopulateMDCWithValues() {
      PowerMockito.mockStatic(MDCUtils.class);
      String messageBody =
              "       {"+
              "     \"eventType\": \"STORE_UPSERT\"," +
              "\"batchId\":\"Sched-2018-04-03T16:20:32.025\","+
              "\"originTimestampInfo\":{\"STORE_UPSERT\":\"2018-04-03T16:20:32.215\"},"+
      "     \"id\": \"106/BL\"," +
      "     \"storeNumber\": \"106\"," +
      "     \"name\": \"Bellevue\"," +
      "     \"addressLine1\": \"11111 NE 8th Street\"," +
      "     \"addressLine2\": \"abc\"," +
      "     \"city\": \"Bellevue\"," +
      "     \"state\": \"WA\"," +
      "     \"zipCode\": \"98004\"," +
      "     \"phoneNumber\": \"425-452-3300\"," +
      "     \"storeHours\": \"Mon. 10:00AM - 8:00PM,Tue. 10:00AM - 8:00PM,Wed. 10:00AM - 8:00PM,Thu. 10:00AM - 8:00PM,Fri. 10:00AM - 8:00PM,Sat. 10:00AM - 8:00PM,Sun. 12:00PM - 6:00PM\"," +
      "     \"storeDescription\": \"<br>Sixthday is a renowned specialty store dedicated to merchandise leadership and superior customer service. We will offer the finest fashion and quality products in a welcoming environment. \"" +
      "     }\"";
      
      when(message.getBody()).thenReturn(messageBody.getBytes());
      storeMessageReceiver.onMessage(message, null);
      verifyStatic(times(1));
      MDCUtils.setMDC("106/BL", String.valueOf(STORE_UPSERT), "Sched-2018-04-03T16:20:32.025", "2018-04-03T16:20:32.215", "StoreQueue", "StoreIndex", Logging.CONTENT_SYNC_sixthday_RESOURCE);
    }
    
    @Test
    public void shouldListenToQueueAndFailToUpdateElasticSearch() throws Exception {
        String messageBody = "ThisIsSomeThingWeDontExpectFromQueue";
        when(message.getBody()).thenReturn(messageBody.getBytes());

        storeMessageReceiver.onMessage(message, null);

        verify(storeSyncService, never()).upsertStore(any(StoreDocument.class), eq(STORE_UPSERT));
    }

    @Test
    public void shouldFailValidationWhenThereIsNoEventType() throws Exception {
        String messageBody = "{ \"storeNumber\": \"106\" }";
        when(message.getBody()).thenReturn(messageBody.getBytes());

        storeMessageReceiver.onMessage(message, null);

        verify(storeSyncService, never()).upsertStore(any(StoreDocument.class), any());
        String actualLog = new String(loggingOutput.toByteArray());
        
        String[] expected = {"StoreMessageReceiverUpdate=\"STORE_MESSAGE_RECEIVED\",",
            "Status=\"Failed\",",
            "MessageId=\"-\",",
            "MessageType=\"null\",",
            "ContextId=\"-\",",
            "OriginTimestamp=\"NA\",",
            "DurationInMs=\"",
            "sixthdaySRC=\"StoreQueue\"","sixthdayDEST=\"StoreIndex\"","sixthdayRESOURCE=\"ElasticSearch\""};
        verifySplunkFieldsOnLog(actualLog, expected);
    }
    
    @Test
    public void shouldSetMinimalValuesToMDCOnMessageFailure() {
      PowerMockito.mockStatic(MDCUtils.class);
      String messageBody = "{ \"id\": \"106\", \"storeNumber\": \"106\" }";
      when(message.getBody()).thenReturn(messageBody.getBytes());
      storeMessageReceiver.onMessage(message, null);
      verifyStatic(times(1));
      MDCUtils.setMDC(eq("106"), eq("null"), isNull(null), eq("NA"), eq("StoreQueue"), eq("StoreIndex"), eq(Logging.CONTENT_SYNC_sixthday_RESOURCE));
    }
    
}

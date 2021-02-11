package com.sixthday.store;

import com.sixthday.store.cloud.stream.StoreSkuInventoryAnnouncer;
import com.sixthday.store.config.Constants.Logging;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryDocument;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryMessage;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryMessage.EventType;
import com.sixthday.store.services.StoreSkuInventorySyncService;
import com.sixthday.store.util.MDCUtils;
import com.sixthday.store.util.sixthdayMDCAdapter;
import com.toggler.core.utils.FeatureToggleRepository;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.amqp.core.Message;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;

import static com.sixthday.store.SplunkFieldsAssertionUtil.verifySplunkFieldsOnLog;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value = MDCUtils.class)
@PowerMockIgnore("javax.management.*")
public class StoreSkuInventoryMessageReceiverTest {

	private static final String STORE_INVENTORY_UPDATED_MSG = "       {" +
    		"			\"storeNumber\": \"01\","+
			"			\"id\": \"sku92740103:01\","+
			"\"batchId\":\"Sched-2018-04-03T16:20:32.025\","+
      "\"originTimestampInfo\":{\"STORE_SKU_INVENTORY_UPSERT\":\"2018-04-03T16:20:32.215\"},"+
			"			\"storeId\": \"01/DT\","+
			"			\"locationNumber\": \"1000\","+
			"			\"quantity\": 1, "+
			"			\"eventType\": \"STORE_SKU_INVENTORY_UPSERT\","+
			"			\"bopsQuantity\": 1,"+
			"			\"inventoryLevelCode\": \"2\","+
			"			\"skuId\": \"sku92740103\""+
			"		}\"";
	
	private static final String STORE_INVENTORY_DELETED_MSG = "       {" +
    		"			\"storeNumber\": \"01\","+
			"			\"id\": \"sku92740103:01\","+
			"\"batchId\":\"Sched-2018-04-03T16:20:32.025\","+
      "\"originTimestampInfo\":{\"STORE_SKU_INVENTORY_DELETE\":\"2018-04-03T16:20:32.215\"},"+	
			"			\"storeId\": \"01/DT\","+
			"			\"locationNumber\": \"1000\","+
			"			\"quantity\": 1, "+
			"			\"eventType\": \"STORE_SKU_INVENTORY_DELETE\","+
			"			\"bopsQuantity\": 1,"+
			"			\"inventoryLevelCode\": \"2\","+
			"			\"skuId\": \"sku92740103\""+
			"		}\"";
	
	private static final String STORE_INVENTORY_INVALID_EVENT_TYPE_MSG = "       {" +
    		"			\"storeNumber\": \"01\","+
			"			\"id\": \"sku92740103:01\","+
			"\"batchId\":\"Sched-2018-04-03T16:20:32.025\","+
      "\"originTimestampInfo\":{\"STORE_SKU_INVENTORY_INVALID_EVENT_TYPE\":\"2018-04-03T16:20:32.215\"},"+
			"			\"storeId\": \"01/DT\","+
			"			\"locationNumber\": \"1000\","+
			"			\"quantity\": 1, "+
			"			\"eventType\": \"STORE_SKU_INVENTORY_INVALID_EVENT_TYPE\","+
			"			\"bopsQuantity\": 1,"+
			"			\"inventoryLevelCode\": \"2\","+
			"			\"skuId\": \"sku92740103\""+
			"		}\"";
	
  private static final String INVALID_JSON_MSG = "ThisIsSomeThingWeDontExpectFromQueue";
  
  private static final String EVENT_TYPE_MISSING_MSG = "{ \"storeNumber\": \"01\" }";
  
  @Mock
  private StoreSkuInventorySyncService storeSkuInventorySyncService;
  
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Message message;
  
  @Mock
  private sixthdayMDCAdapter mdc;
  
  @Mock
  private StoreSkuInventoryAnnouncer announcer;
  
  @InjectMocks
  private StoreSkuInventoryMessageReceiver storeSkuInventoryMessageReceiver;
  
  @Rule
  public FeatureToggleRepository featureToggleRepository = new FeatureToggleRepository();
  
  private ByteArrayOutputStream loggingOutput;
  
  @BeforeClass
  public static void setLoggerContextSelector() {
    System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.selector.BasicContextSelector");
  }
  
  @Before
  @SneakyThrows
  public void setUp() {
    loggingOutput = LogCapture.captureLogOutput(StoreSkuInventoryMessageReceiver.class);
    System.setOut(new PrintStream(loggingOutput));
    FieldUtils.writeField(storeSkuInventoryMessageReceiver, "contentSyncSource", "StoreInventoryQueue", true);
    FieldUtils.writeField(storeSkuInventoryMessageReceiver, "contentSyncDestination", "StoreInventoryIndex", true);
  }
  
  @After
  public void tearDown() {
    LogCapture.stopLogCapture(StoreMessageReceiver.class, loggingOutput);
  }
  
  private void simulateOnMessageForGivenString(String messageBody, String messageType) {
    when(message.getBody()).thenReturn(messageBody.getBytes());
    when(message.getMessageProperties().getType()).thenReturn(messageType);
    storeSkuInventoryMessageReceiver.onMessage(message, null);
  }
  
  @Test
  public void shouldPopulateMDCValuesOnStoreInventoryUpsertMessage() {
    PowerMockito.mockStatic(MDCUtils.class);
    simulateOnMessageForGivenString(STORE_INVENTORY_UPDATED_MSG, EventType.STORE_SKU_INVENTORY_UPSERT.name());
    
    verifyStatic(times(1));
    MDCUtils.setMDC("sku92740103:01", String.valueOf(EventType.STORE_SKU_INVENTORY_UPSERT), "Sched-2018-04-03T16:20:32.025", "2018-04-03T16:20:32.215",
            "StoreInventoryQueue", "StoreInventoryIndex", Logging.CONTENT_SYNC_sixthday_RESOURCE);
  }
  
  
  @Test
  public void shouldListenToQueueAndUpdateElasticSearchWhenUpdateEventTypeOccurs() throws Exception {
    
    simulateOnMessageForGivenString(STORE_INVENTORY_UPDATED_MSG, EventType.STORE_SKU_INVENTORY_UPSERT.name());
    
    StoreSkuInventoryMessage expectedStoreSkuInventoryMessage = constructExpectedStoreSkuInventoryMessage();
    expectedStoreSkuInventoryMessage.setEventType(EventType.STORE_SKU_INVENTORY_UPSERT);
    
    verify(storeSkuInventorySyncService).updateStoreSkuInventory(any(StoreSkuInventoryDocument.class), eq(expectedStoreSkuInventoryMessage));
    
    
    String actualLog = new String(loggingOutput.toByteArray());
    String[] expected = {"StoreSkuInventoryMessageReceiverUpdate=\"STORE_SKU_INVENTORY_MESSAGE_RECEIVED\",",
        "Status=\"Success\",",
        "MessageId=\"sku92740103:01\",",
        "MessageType=\"STORE_SKU_INVENTORY_UPSERT\",",
        "ContextId=\"Sched-2018-04-03T16:20:32.025\",",
        "OriginTimestamp=\"2018-04-03T16:20:32.215\",",
        "DurationInMs=\"",
        "sixthdaySRC=\"StoreInventoryQueue\"", "sixthdayDEST=\"StoreInventoryIndex\"", "sixthdayRESOURCE=\"ElasticSearch\""};
    verifySplunkFieldsOnLog(actualLog, expected);
    
  }
  
  @Test
  public void shouldLogFailedContentSyncLogWhenStoreSkuInventoryUpsertMessageFails() throws Exception {
    Mockito.doThrow(Exception.class).when(storeSkuInventorySyncService).updateStoreSkuInventory(any(), any());
    simulateOnMessageForGivenString(STORE_INVENTORY_UPDATED_MSG, EventType.STORE_SKU_INVENTORY_UPSERT.name());
    
    String actualLog = new String(loggingOutput.toByteArray());
    String[] expected = {"StoreSkuInventoryMessageReceiverUpdate=\"STORE_SKU_INVENTORY_MESSAGE_RECEIVED\",",
        "Status=\"Failed\",",
        "MessageId=\"sku92740103:01\",",
        "MessageType=\"STORE_SKU_INVENTORY_UPSERT\",",
        "ContextId=\"Sched-2018-04-03T16:20:32.025\",",
        "OriginTimestamp=\"2018-04-03T16:20:32.215\",",
        "DurationInMs=\"",
        "sixthdaySRC=\"StoreInventoryQueue\"", "sixthdayDEST=\"StoreInventoryIndex\"", "sixthdayRESOURCE=\"ElasticSearch\""};
    verifySplunkFieldsOnLog(actualLog, expected);
  }
  
  @Test
  public void shouldPopulateMDCValuesOnStoreInventoryDelteMessage() {
    PowerMockito.mockStatic(MDCUtils.class);
    simulateOnMessageForGivenString(STORE_INVENTORY_DELETED_MSG, EventType.STORE_SKU_INVENTORY_DELETE.name());
    
    verifyStatic(times(1));
    MDCUtils.setMDC("sku92740103:01", String.valueOf(EventType.STORE_SKU_INVENTORY_DELETE), "Sched-2018-04-03T16:20:32.025", "2018-04-03T16:20:32.215",
            "StoreInventoryQueue", "StoreInventoryIndex", Logging.CONTENT_SYNC_sixthday_RESOURCE);
  }
  
  @Test
  public void shouldListenToQueueAndUpdateElasticSearchWhenDeleteEventOccurs() throws Exception {
    simulateOnMessageForGivenString(STORE_INVENTORY_DELETED_MSG, EventType.STORE_SKU_INVENTORY_DELETE.name());
    
    StoreSkuInventoryMessage expectedStoreSkuInventoryMessage = constructExpectedStoreSkuInventoryMessage();
    expectedStoreSkuInventoryMessage.setEventType(EventType.STORE_SKU_INVENTORY_DELETE);
    expectedStoreSkuInventoryMessage.setOriginTimestampInfo(Collections.singletonMap("STORE_SKU_INVENTORY_DELETE", "2018-04-03T16:20:32.215"));
    verify(storeSkuInventorySyncService).updateStoreSkuInventory(any(StoreSkuInventoryDocument.class), eq(expectedStoreSkuInventoryMessage));
    
    String actualLog = new String(loggingOutput.toByteArray());
    String[] expected = {"StoreSkuInventoryMessageReceiverUpdate=\"STORE_SKU_INVENTORY_MESSAGE_RECEIVED\",",
        "Status=\"Success\",",
        "MessageId=\"sku92740103:01\",",
        "MessageType=\"STORE_SKU_INVENTORY_DELETE\",",
        "ContextId=\"Sched-2018-04-03T16:20:32.025\",",
        "OriginTimestamp=\"2018-04-03T16:20:32.215\",",
        "DurationInMs=\"",
        "sixthdaySRC=\"StoreInventoryQueue\"", "sixthdayDEST=\"StoreInventoryIndex\"", "sixthdayRESOURCE=\"ElasticSearch\""};
    verifySplunkFieldsOnLog(actualLog, expected);
  }
  
  @Test
  public void shouldLogFailedContentSyncLogWhenStoreSkuInventoryDeleteMessageFails() throws Exception {
    Mockito.doThrow(Exception.class).when(storeSkuInventorySyncService).updateStoreSkuInventory(any(), any());
    simulateOnMessageForGivenString(STORE_INVENTORY_DELETED_MSG, EventType.STORE_SKU_INVENTORY_DELETE.name());
    
    String actualLog = new String(loggingOutput.toByteArray());
    String[] expected = {"StoreSkuInventoryMessageReceiverUpdate=\"STORE_SKU_INVENTORY_MESSAGE_RECEIVED\",",
        "Status=\"Failed\",",
        "MessageId=\"sku92740103:01\",",
        "MessageType=\"STORE_SKU_INVENTORY_DELETE\",",
        "ContextId=\"Sched-2018-04-03T16:20:32.025\",",
        "OriginTimestamp=\"2018-04-03T16:20:32.215\",",
        "DurationInMs=\"",
        "sixthdaySRC=\"StoreInventoryQueue\"", "sixthdayDEST=\"StoreInventoryIndex\"", "sixthdayRESOURCE=\"ElasticSearch\""};
    verifySplunkFieldsOnLog(actualLog, expected);
  }
  
  
  @Test
  public void shouldListenToQueueAndShouldNotUpdateElasticSearchWhenInvalidEventTypeOccurs() throws Exception {
    simulateOnMessageForGivenString(STORE_INVENTORY_INVALID_EVENT_TYPE_MSG, "SomethingInvalid");
    
    StoreSkuInventoryMessage expectedStoreSkuInventoryMessage = constructExpectedStoreSkuInventoryMessage();
    expectedStoreSkuInventoryMessage.setEventType(null);
    
    verify(storeSkuInventorySyncService, never()).updateStoreSkuInventory(any(StoreSkuInventoryDocument.class), eq(expectedStoreSkuInventoryMessage));
  }
  
  private StoreSkuInventoryMessage constructExpectedStoreSkuInventoryMessage() {
    StoreSkuInventoryMessage expectedStoreSkuInventoryMessage = new StoreSkuInventoryMessage();
    expectedStoreSkuInventoryMessage.setId("sku92740103:01");
    expectedStoreSkuInventoryMessage.setBatchId("Sched-2018-04-03T16:20:32.025");
    expectedStoreSkuInventoryMessage.setOriginTimestampInfo(Collections.singletonMap("STORE_SKU_INVENTORY_UPSERT", "2018-04-03T16:20:32.215"));
    expectedStoreSkuInventoryMessage.setStoreNumber("01");
    expectedStoreSkuInventoryMessage.setStoreId("01/DT");
    expectedStoreSkuInventoryMessage.setLocationNumber("1000");
    expectedStoreSkuInventoryMessage.setQuantity(1);
    expectedStoreSkuInventoryMessage.setEventType(EventType.STORE_SKU_INVENTORY_UPSERT);
    expectedStoreSkuInventoryMessage.setBopsQuantity(1);
    expectedStoreSkuInventoryMessage.setInventoryLevelCode("2");
    expectedStoreSkuInventoryMessage.setSkuId("sku92740103");
    return expectedStoreSkuInventoryMessage;
  }
  
  @Test
  public void shouldListenToQueueAndFailToUpdateElasticSearch() throws Exception {
    simulateOnMessageForGivenString(INVALID_JSON_MSG, "SomethingInvalid");
    
    verify(storeSkuInventorySyncService, never()).updateStoreSkuInventory(any(StoreSkuInventoryDocument.class), any(StoreSkuInventoryMessage.class));
  }
  
  @Test
  public void shouldFailValidationWhenThereIsNoEventType() throws Exception {
    simulateOnMessageForGivenString(EVENT_TYPE_MISSING_MSG, null);
    
    verify(storeSkuInventorySyncService, never()).updateStoreSkuInventory(any(StoreSkuInventoryDocument.class), any(StoreSkuInventoryMessage.class));
  }
  
  @Test
  public void shouldAnnounceStoreSkuInventoryUpdate() throws Exception {
    simulateOnMessageForGivenString(STORE_INVENTORY_UPDATED_MSG, EventType.STORE_SKU_INVENTORY_UPSERT.name());
    
    verify(announcer, times(1)).announceStoreSkuInventoryUpdate(any(StoreSkuInventoryMessage.class), eq(false));
  }
}

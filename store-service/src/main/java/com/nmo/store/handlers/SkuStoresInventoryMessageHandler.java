package com.sixthday.store.handlers;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;

import com.sixthday.store.cloud.stream.StoreInventoryBySKUFoundationAnnouncer;
import com.sixthday.store.models.storeinventoryindex.SkuStoresInventoryMessage;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryMessage;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.store.cloud.stream.StoreInventoryBySKUAnnouncer;
import com.sixthday.store.services.StoreSkuInventorySyncService;
import com.sixthday.store.util.MDCUtils;

import lombok.extern.slf4j.Slf4j;

import static com.sixthday.store.config.Constants.Logging.*;

@Component
@Slf4j
public class SkuStoresInventoryMessageHandler {
  private static final String SYNC_SUCCESS_LOG_FORMAT = "sixthdayLogType=\"ContentSyncDashboard\", SkuStoresMsgReceiverUpdate=\"SkuStoresMsgReceived\", Status=\"Success\", DurationInMs=\"{}\"";
  private static final String SYNC_PARSING_ERROR_LOG_FORMAT =
          "sixthdayLogType=\"ContentSyncDashboard\", SkuStoresMsgReceiverUpdate=\"SkuStoresMsgReceived\", Status=\"Failed\", DurationInMs=\"{}\", Error=\"{}\", Message=\"{}\"";
  private static final String SYNC_ERROR_LOG_FORMAT = "sixthdayLogType=\"ContentSyncDashboard\", SkuStoresMsgReceiverUpdate=\"SkuStoresMsgReceived\", Status=\"Failed\", DurationInMs=\"{}\", Error=\"{}\"";
  
  private String contentSyncDestination = "DynDB:"+STORE_INVENTORY_BY_SKU_DYNAMO_TABLE_NAME;
  @Value("RMQ:" + STORE_INVENTORY_BY_SKU_QUEUE_NAME)
  private String contentSyncSource;
  
  private ObjectMapper mapper = new ObjectMapper();
  
  @Autowired
  private StoreSkuInventorySyncService storeSkuInventorySyncService;

  @Value(STORE_PRODUCTION_FOUNDATION_MODE)
  private String productFoundatiosixthdayde;
  
  @Autowired
  private StoreInventoryBySKUAnnouncer announcer;

  @Autowired
  StoreInventoryBySKUFoundationAnnouncer storeInventoryBySKUFoundationAnnouncer;
  
  public void handle(Message message) {
    Instant startTime = Instant.now();
    SkuStoresInventoryMessage inventoryMsg = deserializeMessage(message, startTime);
    if (inventoryMsg != null) {
      try {
        storeSkuInventorySyncService.updateStoreSkuInventory(inventoryMsg);
        logMessageProcessingSuccess(startTime, inventoryMsg);
      } catch (Exception e) {
        String errMsg = e.getMessage();
        log.error(CONTENT_SYNC_LOG_MAKER, SYNC_ERROR_LOG_FORMAT, Duration.between(startTime, Instant.now()).toMillis(), errMsg != null ? errMsg.replaceAll("\"", "'") : null, e);
      }
      announcer.announceStoreSkuInventoryForProductMessage(inventoryMsg);
      if(null!=productFoundatiosixthdayde && "true".equalsIgnoreCase(productFoundatiosixthdayde)) {
        storeInventoryBySKUFoundationAnnouncer.announceStoreSkuFoundationInventoryForProductMessage(inventoryMsg);
      }
    } else {
      // Do nothing
    }
    
  }
  
  private SkuStoresInventoryMessage deserializeMessage(Message message, Instant startTime) {
    String payload = new String(message.getBody());
    SkuStoresInventoryMessage inventoryMsg = null;
    try {
      inventoryMsg = mapper.readValue(payload, SkuStoresInventoryMessage.class);
      String eventType = inventoryMsg.getEventType().name();
      String originTimestamp = Optional.ofNullable(inventoryMsg.getOriginTimestampInfo()).orElse(new HashMap<>()).get(eventType);
      MDCUtils.setMDC(inventoryMsg.getProductId(), eventType, inventoryMsg.getBatchId(), originTimestamp, contentSyncSource, contentSyncDestination, CONTENT_SYNC_AWS_RESOURCE);
    } catch (Exception e) {
      logMessageParsingFailure(startTime, payload, e);
    }
    return inventoryMsg;
  }
  
  private void logMessageProcessingSuccess(Instant start, SkuStoresInventoryMessage inventoryMsg) {
    log.info(CONTENT_SYNC_LOG_MAKER, SYNC_SUCCESS_LOG_FORMAT, Duration.between(start, Instant.now()).toMillis());
  }
  
  private void logMessageParsingFailure(Instant start, String payload, Exception exception) {
    MDCUtils.setMDCOnMessageParsingFailure(contentSyncSource, contentSyncDestination, CONTENT_SYNC_AWS_RESOURCE);
    String errMsg = exception.getMessage();
    log.error("Exception occurred when processing store sku inventory for product", exception);
    log.error(CONTENT_SYNC_LOG_MAKER, SYNC_PARSING_ERROR_LOG_FORMAT, Duration.between(start, Instant.now()).toMillis(), errMsg != null ? errMsg.replaceAll("\"", "'") : null, payload);
  }
}

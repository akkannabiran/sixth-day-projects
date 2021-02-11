package com.sixthday.store.cloud.stream;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.support.MessageBuilder;

import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryAnnouncement;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryMessage;

import lombok.extern.slf4j.Slf4j;

@EnableBinding(value = Source.class)
@Slf4j
public class StoreSkuInventoryAnnouncer {

  private static final String LOG_INFO_FORMAT =
          "StoreSkuInventoryAnnouncerUpdate=\"{}\", sixthdayLogType=\"ContentSyncDashboard\", MessageId=\"{}\", MessageType=\"{}\", ContextId=\"{}\", Status=\"Success\","
          + " SkuId=\"{}\", ProductIds=\"{}\", DurationInMs=\"{}\", OriginTimestamp=\"{}\", sixthdaySRC=\"APP:store-service\", sixthdayDEST=\"{}\", sixthdayRESOURCE=\"-\"";

  private static final String LOG_ERROR_FORMAT =
          "StoreSkuInventoryAnnouncerUpdate=\"{}\", sixthdayLogType=\"ContentSyncDashboard\", MessageId=\"{}\", MessageType=\"{}\", ContextId=\"{}\", Status=\"Failed\","
          + " SkuId=\"{}\", ProductIds=\"{}\", DurationInMs=\"{}\", OriginTimestamp=\"{}\", sixthdaySRC=\"APP:store-service\", sixthdayDEST=\"{}\", sixthdayRESOURCE=\"-\"";

  private Source messageSource;

  @Autowired
  public StoreSkuInventoryAnnouncer(Source messageSource) {
    this.messageSource = messageSource;
  }
  
  @Value("RMQ:${spring.cloud.stream.bindings.output.destination}")
  private String destination;
  
  public StoreSkuInventoryAnnouncement announceStoreSkuInventoryUpdate(StoreSkuInventoryMessage message, boolean removed) {
    Instant start = Instant.now();
    StoreSkuInventoryAnnouncement announcement = new StoreSkuInventoryAnnouncement(message.getProductIds(), message.getSkuId(), message.getStoreNumber(),
            message.getInventoryLevelCode(), message.getQuantity(), removed, message.getBatchId(), message.getOriginTimestampInfo(), message.getDataPoints());
    boolean sent = messageSource.output().send(MessageBuilder.withPayload(announcement).build());
    String updateType = removed ? "InventoryRemoved" : "InventoryUpdated";
    String originTimestamp = Optional.ofNullable(message.getOriginTimestampInfo()).map(e -> e.get(String.valueOf(message.getEventType()))).orElse("NA");
    if (sent) {
      log.info(LOG_INFO_FORMAT, updateType, message.getId(), message.getEventType(), message.getBatchId(), message.getSkuId(), message.getProductIds(),
            Duration.between(start, Instant.now()).toMillis(), originTimestamp, destination);
    } else {
      log.info(LOG_ERROR_FORMAT, updateType, message.getId(), message.getEventType(), message.getBatchId(), message.getSkuId(), message.getProductIds(),
              Duration.between(start, Instant.now()).toMillis(), originTimestamp, destination);
    }
    return announcement;
  }
  
}

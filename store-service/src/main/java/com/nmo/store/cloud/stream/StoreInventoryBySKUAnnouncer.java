package com.sixthday.store.cloud.stream;

import static com.sixthday.store.config.Constants.Logging.NO_MDC_LOG_MARKER;
import static com.sixthday.store.config.Constants.Logging.STORE_INVENTORY_BY_SKU_OUT_STREAM_NAME;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.integration.support.MessageBuilder;

import com.sixthday.store.config.StoreInventoryBySKUSource;
import com.sixthday.store.models.storeinventoryindex.SkuStoresInventoryMessage;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@EnableBinding(value = StoreInventoryBySKUSource.class)
@Slf4j
public class StoreInventoryBySKUAnnouncer {
  @Value("RMQ:"+STORE_INVENTORY_BY_SKU_OUT_STREAM_NAME)
  private String contentSyncDestination;
  
  @Autowired
  @Getter @Setter
  private StoreInventoryBySKUSource messageSource;
  
  private static final String LOG_INFO_FORMAT = "SkuStoresMsgAnnouncerUpdate=\"SkuStoresMsgPublished\", sixthdayLogType=\"ContentSyncDashboard\", MessageId=\"{}\", MessageType=\"{}\", ContextId=\"{}\", "
          + "Status=\"Success\", DurationInMs=\"{}\", OriginTimestamp=\"{}\", sixthdaySRC=\"APP:store-service\", sixthdayDEST=\"{}\", sixthdayRESOURCE=\"-\"";
  private static final String LOG_ERROR_FORMAT = "SkuStoresMsgAnnouncerUpdate=\"SkuStoresMsgPublished\", sixthdayLogType=\"ContentSyncDashboard\", MessageId=\"{}\", MessageType=\"{}\", ContextId=\"{}\", "
          + "Status=\"Failed\", DurationInMs=\"{}\", OriginTimestamp=\"{}\", sixthdaySRC=\"APP:store-service\", sixthdayDEST=\"{}\", sixthdayRESOURCE=\"-\"";
  
  public SkuStoresInventoryMessage announceStoreSkuInventoryForProductMessage(SkuStoresInventoryMessage message) {
    Instant start = Instant.now();
    String originTimestamp = Optional.ofNullable(message.getOriginTimestampInfo()).map(e -> e.get(String.valueOf(message.getEventType()))).orElse("NA");
    boolean sent = messageSource.storeInventoryBySKUChannel().send(MessageBuilder.withPayload(message).build());
    if (sent) {
      log.info(NO_MDC_LOG_MARKER, LOG_INFO_FORMAT, message.getProductId(), message.getEventType(), message.getBatchId(), Duration.between(start, Instant.now()).toMillis(), originTimestamp, contentSyncDestination);
    } else {
      log.error(NO_MDC_LOG_MARKER, LOG_ERROR_FORMAT, message.getProductId(), message.getEventType(), message.getBatchId(), Duration.between(start, Instant.now()).toMillis(), originTimestamp, contentSyncDestination);
    }
    return message;
  }
  
}

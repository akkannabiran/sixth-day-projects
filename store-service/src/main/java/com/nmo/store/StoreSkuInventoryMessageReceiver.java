package com.sixthday.store;

import static com.sixthday.store.config.Constants.Logging.CONTENT_SYNC_LOG_MAKER;
import static com.sixthday.store.config.Constants.Logging.CONTENT_SYNC_sixthday_RESOURCE;
import static com.sixthday.store.config.Constants.Logging.STORE_INVENTORY_INDEX_NAME;
import static com.sixthday.store.config.Constants.Logging.STORE_INVENTORY_QUEUE_NAME;
import static com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryMessage.EventType.STORE_SKU_INVENTORY_UPSERT;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.store.cloud.stream.StoreSkuInventoryAnnouncer;
import com.sixthday.store.exceptions.StoreSkuInventorySycFailedException;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryDocument;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryMessage;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryMessage.EventType;
import com.sixthday.store.services.StoreSkuInventorySyncService;
import com.sixthday.store.util.MDCUtils;
import com.sixthday.store.util.sixthdayMDCAdapter;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@ComponentScan(basePackages = { "com.sixthday.storeinventory" })
public class StoreSkuInventoryMessageReceiver implements ChannelAwareMessageListener {
	private static final String SYNC_SUCCESS_LOG_FORMAT = "sixthdayLogType=\"ContentSyncDashboard\", StoreSkuInventoryMessageReceiverUpdate=\"STORE_SKU_INVENTORY_MESSAGE_RECEIVED\", Status=\"Success\", DurationInMs=\"{}\"";
  private static final String SYNC_ERROR_LOG_FORMAT = "sixthdayLogType=\"ContentSyncDashboard\", StoreSkuInventoryMessageReceiverUpdate=\"STORE_SKU_INVENTORY_MESSAGE_RECEIVED\", Status=\"Failed\", DurationInMs=\"{}\", Error=\"{}\"";
	private StoreSkuInventorySyncService storeSkuInventorySyncService;
	private sixthdayMDCAdapter mdc;
	private StoreSkuInventoryAnnouncer announcer;
	private int storeSkuInventoryUpdateMsgCount = 0;
	private int storeSkuInventoryDeleteMsgCount = 0;
	private int unKnownMsgCount = 0;
	private ObjectMapper mapper = new ObjectMapper();
	
	@Value("ES:" + STORE_INVENTORY_INDEX_NAME)
  private String contentSyncDestination;
  @Value("RMQ:" + STORE_INVENTORY_QUEUE_NAME)
  private String contentSyncSource;

	@Autowired
	public StoreSkuInventoryMessageReceiver(StoreSkuInventorySyncService storeSkuInventorySyncService, sixthdayMDCAdapter mdc, StoreSkuInventoryAnnouncer announcer) {
		this.storeSkuInventorySyncService = storeSkuInventorySyncService;
		this.mdc = mdc;
		this.announcer = announcer;
	}

	@Override
	public void onMessage(Message message, Channel channel) {
		Instant startTime = Instant.now();
		String storeSkuInventoryId = null;
		String eventType = null;
		String contextId = null;
		try {
		  eventType =  message.getMessageProperties().getType();
			StoreSkuInventoryMessage storeSkuInventoryMessage = mapper.readValue(message.getBody(), StoreSkuInventoryMessage.class);
			contextId = storeSkuInventoryMessage.getBatchId();
      storeSkuInventoryId = storeSkuInventoryMessage.getId();
      String originTimestamp = Optional.ofNullable(storeSkuInventoryMessage.getOriginTimestampInfo()).map(e -> e.get(String.valueOf(storeSkuInventoryMessage.getEventType()))).orElse("NA");
      
      MDCUtils.setMDC(storeSkuInventoryId, eventType, storeSkuInventoryMessage.getBatchId(), originTimestamp, contentSyncSource, contentSyncDestination, CONTENT_SYNC_sixthday_RESOURCE);
      
			validate(storeSkuInventoryMessage);

			StoreSkuInventoryDocument storeSkuInventoryDocument = new StoreSkuInventoryMessageMapper().map(storeSkuInventoryMessage);

			log.debug("StoreSkuInventoryMessageReceiverUpdate=\"STORE_SKU_INVENTORY_MESSAGE_RECEIVED\", Status=\"Begin\", Message=\"{}\"", storeSkuInventoryMessage);

			storeSkuInventorySyncService.updateStoreSkuInventory(storeSkuInventoryDocument, storeSkuInventoryMessage);

			announcer.announceStoreSkuInventoryUpdate(storeSkuInventoryMessage, EventType.STORE_SKU_INVENTORY_DELETE.name().equals(eventType));

			incrementMessagesCount(storeSkuInventoryMessage.getEventType());
			logStoreSkuInventoryMessageDetails(startTime);
		} catch (Exception e) {
			String errMsg = e.getMessage();
			log.error(CONTENT_SYNC_LOG_MAKER, SYNC_ERROR_LOG_FORMAT,  Duration.between(startTime, Instant.now()).toMillis(), errMsg != null ? errMsg.replaceAll("\"", "'") : null);
			log.debug(
					"StoreSkuInventoryMessageReceiverUpdate=\"STORE_SKU_INVENTORY_MESSAGE_RECEIVED_ERROR_DETAIL\", MessageId=\"{}\", MessageType=\"{}\", ContextId=\"{}\", DurationInMs=\"{}\", Error=\"{}\", Message=\"{}\"",
					storeSkuInventoryId, eventType, contextId, Duration.between(startTime, Instant.now()).toMillis(), e,
					new String(message.getBody()).replaceAll("\"", ""));
		} finally {
			mdc.clear();
		}
	}

	private void logStoreSkuInventoryMessageDetails(Instant start) {
	  log.info(CONTENT_SYNC_LOG_MAKER, SYNC_SUCCESS_LOG_FORMAT, Duration.between(start, Instant.now()).toMillis());
		log.debug(
				"StoreSkuInventoryMessageReceiverUpdate=\"TOTAL_MESSAGES_PROCESSED\". StoreSkuInventoryUpdated={}, StoreSkuInventoryDeleted={}, unKnownMsgCount={}",
				storeSkuInventoryUpdateMsgCount, storeSkuInventoryDeleteMsgCount, unKnownMsgCount);
	}

	private void validate(StoreSkuInventoryMessage storeSkuInventoryMessage)
			throws StoreSkuInventorySycFailedException {
		if (storeSkuInventoryMessage.isInvalid()) {
			throw new StoreSkuInventorySycFailedException(storeSkuInventoryMessage,
					"EventType is not handled. eventType=" + storeSkuInventoryMessage.getEventType());
		}
	}

	private void incrementMessagesCount(final EventType eventType) {
		if (eventType.equals(STORE_SKU_INVENTORY_UPSERT)) {
			storeSkuInventoryUpdateMsgCount++;
		} else {
			storeSkuInventoryDeleteMsgCount++;
		}
	}
}

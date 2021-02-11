package com.sixthday.store;

import static com.sixthday.store.config.Constants.Logging.CONTENT_SYNC_LOG_MAKER;
import static com.sixthday.store.config.Constants.Logging.CONTENT_SYNC_sixthday_RESOURCE;
import static com.sixthday.store.config.Constants.Logging.STORE_INDEX_NAME;
import static com.sixthday.store.config.Constants.Logging.STORE_QUEUE_NAME;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.store.exceptions.StoreUpsertFailedException;
import com.sixthday.store.models.storeindex.StoreDocument;
import com.sixthday.store.models.storeindex.StoreMessage;
import com.sixthday.store.services.StoreSyncService;
import com.sixthday.store.util.MDCUtils;
import com.sixthday.store.util.sixthdayMDCAdapter;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StoreMessageReceiver implements ChannelAwareMessageListener {
	private static final String SYNC_SUCCESS_LOG_FORMAT = "sixthdayLogType=\"ContentSyncDashboard\", StoreMessageReceiverUpdate=\"STORE_MESSAGE_RECEIVED\", Status=\"Success\", DurationInMs=\"{}\"";
  private static final String SYNC_ERROR_LOG_FORMAT = "sixthdayLogType=\"ContentSyncDashboard\", StoreMessageReceiverUpdate=\"STORE_MESSAGE_RECEIVED\", Status=\"Failed\", DurationInMs=\"{}\", Error=\"{}\"";
	private StoreSyncService storeSyncService;
	private sixthdayMDCAdapter mdc;
	private int storeMessagesCount = 0;
	private int unKnownMessagesCount = 0;
	private ObjectMapper mapper = new ObjectMapper();
	
	@Value("ES:" + STORE_INDEX_NAME)
	private String contentSyncDestination;
  @Value("RMQ:" + STORE_QUEUE_NAME)
  private String contentSyncSource;
  
  
	@Autowired
	public StoreMessageReceiver(StoreSyncService storeSyncService, sixthdayMDCAdapter mdc) {
		this.storeSyncService = storeSyncService;
		this.mdc = mdc;
	}

	@Override
	public void onMessage(Message message, Channel channel) {
		Instant startTime = Instant.now();
		String storeId = null;
		String eventType = null;

		try {
			StoreMessage storeMessage = mapper.readValue(message.getBody(), StoreMessage.class);
			eventType = String.valueOf(storeMessage.getEventType());
      String originTimestamp = Optional.ofNullable(storeMessage.getOriginTimestampInfo()).map(e -> e.get(String.valueOf(storeMessage.getEventType()))).orElse("NA");
      
			MDCUtils.setMDC(storeMessage.getId(), eventType, storeMessage.getBatchId(), originTimestamp, contentSyncSource, contentSyncDestination, CONTENT_SYNC_sixthday_RESOURCE);
			
			validate(storeMessage);

			StoreDocument storeDocument = new StoreMessageMapper().map(storeMessage);
			log.debug("StoreMessageReceiverUpdate=\"STORE_MESSAGE_RECEIVED\", Status=\"Begin\", Message=\"{}\"",
					storeMessage);
			storeId = storeDocument.getId();

			upsertStore(storeMessage, storeDocument);

			incrementMessagesCount(storeMessage.getEventType());
			logStoreMessageUpdated(startTime);
		} catch (Exception e) {
			String errMsg = e.getMessage();
			log.error(CONTENT_SYNC_LOG_MAKER, SYNC_ERROR_LOG_FORMAT,  Duration.between(startTime, Instant.now()).toMillis(), errMsg != null ? errMsg.replaceAll("\"", "'") : null);
			log.debug(
					"StoreMessageReceiverUpdate=\"STORE_MESSAGE_RECEIVED_ERROR_DETAIL\", MessageId=\"{}\", MessageType=\"{}\", DurationInMs=\"{}\", Error=\"{}\", Message=\"{}\"",
					storeId, eventType, Duration.between(startTime, Instant.now()).toMillis(), e,
					new String(message.getBody()).replaceAll("\"", ""));
		} finally {
			mdc.clear();
		}
	}

	private void logStoreMessageUpdated(Instant start) {
	  log.info(CONTENT_SYNC_LOG_MAKER, SYNC_SUCCESS_LOG_FORMAT, Duration.between(start, Instant.now()).toMillis());
	  
		log.debug("StoreMessageReceiverUpdate=\"TOTAL_MESSAGES_PROCESSED\". Store={}, Unknown={}", storeMessagesCount,
				unKnownMessagesCount);
	}

	private void upsertStore(StoreMessage storeMessage, StoreDocument storeDocument) throws StoreUpsertFailedException {
		storeSyncService.upsertStore(storeDocument, storeMessage.getEventType());
	}

	private void validate(StoreMessage storeMessage) throws StoreUpsertFailedException {
		if (storeMessage.isInvalid()) {
			throw new StoreUpsertFailedException(storeMessage,
					"EventType is not handled on ElasticSearchIndexUpdater. eventType=" + storeMessage.getEventType());
		}
	}

	private void incrementMessagesCount(final StoreMessage.EventType eventType) {
		switch (eventType) {
		case STORE_UPSERT:
			storeMessagesCount++;
			break;
		default:
			unKnownMessagesCount++;
		}
	}
	
}

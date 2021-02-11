package com.sixthday.navigation.integration.receiver;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.integration.config.PublisherConfiguration;
import com.sixthday.navigation.integration.mappers.CategoryMessageMapper;
import com.sixthday.navigation.integration.messages.CategoryMessage;
import com.sixthday.navigation.integration.services.*;
import com.sixthday.navigation.integration.utils.MDCUtils;
import com.rabbitmq.client.Channel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static com.sixthday.navigation.config.Constants.CONTENT_SYNC_ES_RESOURCE;
import static com.sixthday.navigation.config.Constants.CONTENT_SYNC_LOG_MAKER;

@Getter
@Component
@Slf4j
public class CategoryMessageReceiver implements ChannelAwareMessageListener {
    private static final String CONTENT_SYNC_NMOSRC = "RMQ:sixthday-category";
    private static final String CONTENT_SYNC_NMODEST = "ES:category_index";
    private static final String SYNC_SUCCESS_LOG_FORMAT = "NMOLogType=\"ContentSyncDashboard\", CategoryMessageReceiverUpdate=\"CATEGORY_MESSAGE_RECEIVED\", Status=\"Success\", DurationInMs=\"{}\"";
    private static final String SYNC_ERROR_LOG_FORMAT = "NMOLogType=\"ContentSyncDashboard\", CategoryMessageReceiverUpdate=\"CATEGORY_MESSAGE_RECEIVED\", Status=\"Failed\", DurationInMs=\"{}\", Error=\"{}\", Message=\"{}\"";
    private static final String MESSAGE_TYPE = "Category Message";

    private CategorySyncService categorySyncService;
    private LeftNavSyncService leftNavSyncService;
    private CategoryPublisherService categoryPublisherService;
    private PublisherConfiguration publisherConfiguration;
    private int categoryMessagesCount = 0;
    private int unknownMessages = 0;
    private ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    public CategoryMessageReceiver(CategorySyncService categorySyncService, CategoryPublisherService categoryPublisherService,
                                   PublisherConfiguration publisherConfiguration, LeftNavSyncService leftNavSyncService) {
        this.categorySyncService = categorySyncService;
        this.categoryPublisherService = categoryPublisherService;
        this.publisherConfiguration = publisherConfiguration;
        this.leftNavSyncService = leftNavSyncService;
    }

    @Override
    public void onMessage(Message message, Channel channel) {
        Instant start = Instant.now();
        String categoryId = null;
        String originTimestamp = null;
        String batchId = null;
        try {
            MDC.put("messageType", MESSAGE_TYPE);
            CategoryMessage categoryMessage = deserializeCategoryMessage(message, start);

            if (categoryMessage != null) {
                if (categoryMessage.getEventType() == null) {
                    log.error("eventType Not Found");
                    return;
                }

                CategoryDocument categoryDocument = new CategoryMessageMapper().map(categoryMessage);
                categoryId = categoryDocument.getId();
                batchId = categoryMessage.getBatchId();
                originTimestamp = Optional.ofNullable(categoryMessage.getOriginTimestampInfo()).map(e -> e.get(String.valueOf(MESSAGE_TYPE))).orElse("NA");

                MDCUtils.setMDC(categoryId, MESSAGE_TYPE, batchId, originTimestamp, CONTENT_SYNC_NMOSRC, CONTENT_SYNC_NMODEST, CONTENT_SYNC_ES_RESOURCE);

                upsertOrDeleteCategory(categoryMessage, categoryDocument);
                incrementMessagesCount(categoryMessage.getEventType());
                log.info(CONTENT_SYNC_LOG_MAKER, SYNC_SUCCESS_LOG_FORMAT, Duration.between(start, Instant.now()).toMillis());
            }
        } catch (Exception e) {
            String errMsg = e.getMessage();
            log.error(CONTENT_SYNC_LOG_MAKER, SYNC_ERROR_LOG_FORMAT, Duration.between(start, Instant.now()).toMillis(), errMsg != null ? errMsg.replaceAll("\"", "'") : null, new String(Optional.ofNullable(message.getBody()).orElse(new byte[0])), e);
        } finally {
            MDC.clear();
        }
    }

    private CategoryMessage deserializeCategoryMessage(Message message, Instant start) {
        CategoryMessage categoryMessage = null;
        try {
            MDCUtils.setMDCOnMessageParsingFailure(CONTENT_SYNC_NMOSRC, CONTENT_SYNC_NMODEST, CONTENT_SYNC_ES_RESOURCE);
            categoryMessage = mapper.readValue(message.getBody(), CategoryMessage.class);
        } catch (Exception e) {
            log.error(CONTENT_SYNC_LOG_MAKER, SYNC_ERROR_LOG_FORMAT, Duration.between(start, Instant.now()).toMillis(), e.getMessage(), new String(message.getBody()), e);
        }
        return categoryMessage;
    }

    private void upsertOrDeleteCategory(CategoryMessage categoryMessage, CategoryDocument categoryDocument) {
        categorySyncService.upsertOrDeleteCategory(categoryDocument, categoryMessage.getEventType());
        if (categoryDocument.isDeleted()) {
            leftNavSyncService.deleteLeftNavTreeForThatCategory(categoryDocument.getId());
        }
    }

    private void incrementMessagesCount(final CategoryMessage.EventType eventType) {
        switch (eventType) {
            case CATEGORY_UPDATED:
            case CATEGORY_REMOVED:
                categoryMessagesCount++;
                break;
            default:
                unknownMessages++;
        }
    }
}

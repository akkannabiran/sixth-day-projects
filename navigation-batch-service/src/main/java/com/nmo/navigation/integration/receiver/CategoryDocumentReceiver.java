package com.sixthday.navigation.integration.receiver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.navigation.batch.designers.processor.DesignerIndexProcessor;
import com.sixthday.navigation.batch.processor.LeftNavTreeProcessor;
import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import com.sixthday.navigation.integration.messages.CategoryMessage;
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
import java.util.Collections;
import java.util.Optional;

import static com.sixthday.sixthdayLogging.*;
import static com.sixthday.sixthdayLogging.EventType.DTMESSAGE;
import static com.sixthday.sixthdayLogging.OperationType.CATEGORY_DOCUMENT_MESSAGE_RECEIVED;

@Getter
@Component
@Slf4j
public class CategoryDocumentReceiver implements ChannelAwareMessageListener {
    private static final String CONTENT_SYNC_sixthdayDEST = "ES:leftnav_index";
    private static final String SYNC_SUCCESS_LOG_FORMAT = "sixthdayLogType=\"ContentSyncDashboard\", CategoryDocumentReceiverUpdate=\"CATEGORY_DOCUMENT_MESSAGE_RECEIVED\", Status=\"Success\", DurationInMs=\"{}\"";
    private static final String SYNC_ERROR_LOG_FORMAT = "sixthdayLogType=\"ContentSyncDashboard\", CategoryDocumentReceiverUpdate=\"CATEGORY_DOCUMENT_MESSAGE_RECEIVED\", Status=\"Failed\", DurationInMs=\"{}\", Error=\"{}\", Message=\"{}\"";
    private static final String MESSAGE_TYPE = "Category Message";

    private int categoryMessagesCount = 0;
    private int unknownMessages = 0;

    private ObjectMapper mapper = new ObjectMapper();

    private LeftNavTreeProcessor leftNavTreeProcessor;
    private DesignerIndexProcessor designerIndexProcessor;
    private NavigationBatchServiceConfig navigationBatchServiceConfig;

    @Autowired
    public CategoryDocumentReceiver(LeftNavTreeProcessor leftNavTreeProcessor, DesignerIndexProcessor designerIndexProcessor, NavigationBatchServiceConfig navigationBatchServiceConfig) {
        this.leftNavTreeProcessor = leftNavTreeProcessor;
        this.designerIndexProcessor = designerIndexProcessor;
        this.navigationBatchServiceConfig = navigationBatchServiceConfig;
    }

    @Override
    public void onMessage(Message message, Channel channel) {
        String queueName = navigationBatchServiceConfig.getRabbitmqConfig().getReceiver().getCategoryEvent().getQueueName();
        Instant start = Instant.now();
        try {
            MDC.put("messageType", "Category Document Message");
            CategoryMessage categoryDocument = deserializeCategoryMessage(message, start, "RMQ:" + queueName);
            if (categoryDocument != null) {
                String categoryId = categoryDocument.getId();
                MDC.put("MessageId", categoryId);
                String batchId = categoryDocument.getBatchId();
                String originTimestamp = Optional.ofNullable(categoryDocument.getOriginTimestampInfo()).map(e -> e.get(String.valueOf(MESSAGE_TYPE))).orElse("NA");

                MDCUtils.setMDC(categoryId, MESSAGE_TYPE, batchId, originTimestamp, "RMQ:" + queueName, CONTENT_SYNC_sixthdayDEST, CONTENT_SYNC_ES_RESOURCE);

                int messageCount = channel.queueDeclare(queueName, true, false, false, Collections.emptyMap()).getMessageCount();
                int tlvToIncludeReferenceIdsPathToRebuild = navigationBatchServiceConfig.getLeftNavBatchConfig().getTlvToIncludeReferenceIdsPathToRebuild();
                boolean includeReferenceIdsPathToRebuild = messageCount <= tlvToIncludeReferenceIdsPathToRebuild;

                if (designerIndexProcessor.isRebuildDesignerIndex(categoryDocument)) {
                    designerIndexProcessor.buildDesignerIndex();
                }

                logDebugOperation(log, DTMESSAGE, CATEGORY_DOCUMENT_MESSAGE_RECEIVED, () -> {
                    if (navigationBatchServiceConfig.getLeftNavBatchConfig().isBuildOnEventReceiver()) {
                        leftNavTreeProcessor.startByEvent(categoryDocument, includeReferenceIdsPathToRebuild);
                    }
                    return null;
                });
                log.info(CONTENT_SYNC_LOG_MAKER, SYNC_SUCCESS_LOG_FORMAT, Duration.between(start, Instant.now()).toMillis());
            }
        } catch (Exception e) {
            String errMsg = e.getMessage();
            log.error(CONTENT_SYNC_LOG_MAKER, SYNC_ERROR_LOG_FORMAT, Duration.between(start, Instant.now()).toMillis(), errMsg != null ? errMsg.replaceAll("\"", "'") : null, new String(Optional.ofNullable(message.getBody()).orElse(new byte[0])));
            logError(log, DTMESSAGE, CATEGORY_DOCUMENT_MESSAGE_RECEIVED, "Failed to process category document message", e);
        } finally {
            MDC.clear();
        }
    }

    private CategoryMessage deserializeCategoryMessage(Message message, Instant start, String queueName) {
        CategoryMessage categoryMessage = null;
        try {
            MDCUtils.setMDCOnMessageParsingFailure(queueName, CONTENT_SYNC_sixthdayDEST, CONTENT_SYNC_ES_RESOURCE);
            categoryMessage = mapper.readValue(message.getBody(), CategoryMessage.class);
        } catch (Exception e) {
            log.error(CONTENT_SYNC_LOG_MAKER, SYNC_ERROR_LOG_FORMAT, Duration.between(start, Instant.now()).toMillis(), e.getMessage(), new String(message.getBody()), e);
        }
        return categoryMessage;
    }
}

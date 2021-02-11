package com.sixthday.navigation.integration.publisher;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.logger.logging.LoggableEvent;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.config.Constants;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import static com.sixthday.navigation.config.Constants.*;

@Component
@Slf4j
public class CategoryMessagePublisher {
    private static final String BASE_LOG_MSG = "NMOLogType=\"ContentSyncDashboard\", CategoryMessagePublisherUpdate=\"CATEGORY_MESSAGE_PUBLISHED\", MessageId=\"{}\", MessageType=\"Category Message\", ContextId=\"{}\", OriginTimestamp=\"{}\", NMOSRC=\"APP:navigation-service\", NMODEST=\"RMQ:navigation-service.category\", NMORESOURCE=\"-\", DurationInMs=\"{}\", ";
    private static final String SYNC_SUCCESS_LOG_FORMAT = BASE_LOG_MSG + "Status=\"Success\"";
    private static final String SYNC_ERROR_LOG_FORMAT = BASE_LOG_MSG + "Status=\"Failed\", Error=\"{}\"";

    @Qualifier(value = "categoryEventPublisherRabbitTemplate")
    private RabbitTemplate rabbitTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public CategoryMessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @SneakyThrows
    @LoggableEvent(eventType = Constants.EVENT_MESSAGE, action = Constants.PUBLISH_CATEGORY_MESSAGE_EVENT)
    public void sendMessage(CategoryDocument categoryDocument) {
        Instant start = Instant.now();
        try {
            NavigationMessage navigationMsg = new NavigationMessage();
            navigationMsg.setCategoryDocument(categoryDocument);
            navigationMsg.setBatchId(MDC.get(MDC_CONTEXT_ID));
            navigationMsg.setOriginTimestampInfo(Collections.singletonMap("Category Message", MDC.get(MDC_ORIGIN_TIMESTAMP)));
            rabbitTemplate.convertAndSend(objectMapper.writeValueAsString(navigationMsg));
            log.info(NO_MDC_LOG_MARKER, SYNC_SUCCESS_LOG_FORMAT, categoryDocument.getId(), MDC.get(MDC_CONTEXT_ID), MDC.get(MDC_ORIGIN_TIMESTAMP), Duration.between(start, Instant.now()).toMillis());
        } catch (Exception e) {
            log.info(NO_MDC_LOG_MARKER, SYNC_ERROR_LOG_FORMAT, categoryDocument.getId(), MDC.get(MDC_CONTEXT_ID), MDC.get(MDC_ORIGIN_TIMESTAMP), Duration.between(start, Instant.now()).toMillis(), e.getMessage());
            throw e;
        }
    }

    @Setter
    @Getter
    @NoArgsConstructor
    private static class NavigationMessage {
        private String batchId;
        private Map<String, String> originTimestampInfo;
        @JsonUnwrapped
        private CategoryDocument categoryDocument;
    }

}

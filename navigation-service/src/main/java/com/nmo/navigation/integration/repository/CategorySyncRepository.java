package com.sixthday.navigation.integration.repository;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.logger.logging.LoggableEvent;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.config.Constants;
import com.sixthday.navigation.integration.config.PublisherConfiguration;
import com.sixthday.navigation.integration.config.SubscriberConfiguration;
import com.sixthday.navigation.integration.exceptions.CategoryMessageProcessingException;
import com.sixthday.navigation.integration.messages.CategoryMessage;
import com.sixthday.navigation.integration.services.CategoryPublisherService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static com.sixthday.navigation.config.Constants.*;

@Slf4j
@Component
public class CategorySyncRepository {
    private static final String SUCCESS_LOG_FORMAT = "NMOLogType=\"ContentSyncDashboard\", OperationType=\"ES_UPDATE\", Status=\"Success\", DurationInMs=\"{}\"";
    private static final String FAILURE_LOG_FORMAT = "NMOLogType=\"ContentSyncDashboard\", OperationType=\"ES_UPDATE\", Status=\"Failed\", DurationInMs=\"{}\", Error=\"{}\"";
    private RestHighLevelClient restHighLevelClient;
    private ObjectMapper objectMapper;
    private SubscriberConfiguration subscriberConfiguration;
    private PublisherConfiguration publisherConfiguration;
    private CategoryPublisherService categoryPublisherService;

    @Autowired
    public CategorySyncRepository(final SubscriberConfiguration subscriberConfiguration, PublisherConfiguration publisherConfiguration, CategoryPublisherService categoryPublisherService) {
        this.subscriberConfiguration = subscriberConfiguration;
        this.restHighLevelClient = subscriberConfiguration.elasticSearchClient();
        this.publisherConfiguration = publisherConfiguration;
        this.categoryPublisherService = categoryPublisherService;
        this.objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @LoggableEvent(eventType = Constants.REPOSITORY, action = Constants.SAVE_CATEGORY_DOCUMENT_TO_ES)
    public void upsertOrDeleteCategoryDocument(CategoryDocument categoryDocument, CategoryMessage.EventType eventType) {
        BulkRequest bulkRequest = prepareRequestBuilder(categoryDocument, eventType);
        executeAsyncRequest(bulkRequest, categoryDocument);
    }

    private void executeAsyncRequest(BulkRequest bulkRequest, final CategoryDocument categoryDocument) {
        Instant start = Instant.now();
        try {
            BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            MDC.setContextMap(contextMap);
            Instant end = Instant.now();
            handleOnResponse(bulkResponse, end, start, categoryDocument);

        } catch (Exception e) {
            setMDC(categoryDocument.getId());
            log.error(CONTENT_SYNC_LOG_MAKER, FAILURE_LOG_FORMAT, Duration.between(start, Instant.now()).toMillis(), e.getMessage(), e);
            log.error(ACTION + ELASTICSEARCH_ASYNC_UPDATE_CATEGORY_DOCUMENT + "\" Error caught in @ExceptionHandler", e);
        }
    }

    private void handleOnResponse(BulkResponse updateResponse, Instant end, Instant start, CategoryDocument categoryDocument) {
        if (updateResponse.hasFailures()) {
            log.error(CONTENT_SYNC_LOG_MAKER, FAILURE_LOG_FORMAT, Duration.between(start, end).toMillis(), updateResponse.buildFailureMessage());
            log.error(ACTION + ELASTICSEARCH_ASYNC_UPDATE_CATEGORY_DOCUMENT + "\" Error caught in @ExceptionHandler", updateResponse.buildFailureMessage());
        } else {
            log.info(CONTENT_SYNC_LOG_MAKER, SUCCESS_LOG_FORMAT, Duration.between(start, end).toMillis());
            if (publisherConfiguration.getRabbitmqConfig().getPublisher().getCategoryEvent().isEnabled() && !categoryDocument.isDeleted()) {
                categoryPublisherService.buildAndSend(categoryDocument);
            }
        }
    }

    @SneakyThrows
    private BulkRequest prepareRequestBuilder(final CategoryDocument categoryDocument, CategoryMessage.EventType eventType) {
        BulkRequest bulkRequest = new BulkRequest();
        switch (eventType) {
            case CATEGORY_UPDATED:
            case CATEGORY_REMOVED:
                if (categoryDocument.isDeleted())
                    bulkRequest.add(prepareCategoryDeleteRequest(categoryDocument));
                else
                    bulkRequest.add(prepareCategoryUpsertRequest(categoryDocument));
                break;
            default:
                throw new CategoryMessageProcessingException(categoryDocument, "EventType is not handled on ElasticSearchIndexUpdater. eventType=" + eventType);
        }
        return bulkRequest;
    }

    @SneakyThrows
    private UpdateRequest prepareCategoryUpsertRequest(final CategoryDocument categoryDocument) {
        UpdateRequest updateRequest = new UpdateRequest(subscriberConfiguration.getElasticSearchConfig().getIndexName(), CategoryDocument.DOCUMENT_TYPE, categoryDocument.getId());
        updateRequest.doc(objectMapper.writeValueAsString(categoryDocument), XContentType.JSON)
                .docAsUpsert(true);
        return updateRequest;

    }

    private DeleteRequest prepareCategoryDeleteRequest(final CategoryDocument categoryDocument) {
        return new DeleteRequest(subscriberConfiguration.getElasticSearchConfig().getIndexName(), subscriberConfiguration.getElasticSearchConfig().getDocumentType6(), categoryDocument.getId());
    }

    private void setMDC(String categoryId) {
        MDC.put("MessageId", categoryId);
    }
}

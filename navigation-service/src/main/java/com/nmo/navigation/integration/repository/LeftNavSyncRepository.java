package com.sixthday.navigation.integration.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.logger.logging.LoggableEvent;
import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.config.Constants;
import com.sixthday.navigation.elasticsearch.documents.LeftNavDocument;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static com.sixthday.navigation.config.Constants.*;

@Slf4j
@Component
public class LeftNavSyncRepository {

    private static final String SUCCESS_LOG_FORMAT = "LeftNavSyncRepositoryUpdate=\"Delete\", NMOLogType=\"ContentSyncDashboard\", OperationType=\"ES_UPDATE\", Status=\"Success\", DurationInMs=\"{}\", LeftNavIds=\"{}\"";
    private static final String FAILURE_LOG_FORMAT = "LeftNavSyncRepositoryUpdate=\"Delete\", NMOLogType=\"ContentSyncDashboard\", OperationType=\"ES_UPDATE\", Status=\"Failed\", DurationInMs=\"{}\", LeftNavIds=\"{}\", Error=\"{}\"";

    private NavigationServiceConfig navigationServiceConfig;
    private ObjectMapper objectMapper;
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    public LeftNavSyncRepository(NavigationServiceConfig navigationServiceConfig, ObjectMapper objectMapper, RestHighLevelClient restHighLevelClient) {
        this.navigationServiceConfig = navigationServiceConfig;
        this.objectMapper = objectMapper;
        this.restHighLevelClient = restHighLevelClient;
    }

    @PreDestroy
    @SneakyThrows
    public void closeClient() {
        restHighLevelClient.close();
    }

    @LoggableEvent(eventType = Constants.REPOSITORY, action = Constants.GET_LEFTNAV_DOCUMENT_FROM_ES)
    public void fetchAndDeleteLeftNavDocument(String categoryId) {

        SearchResponse searchResponse = buildLeftNavSearchRequest(categoryId);
        List<LeftNavDocument> leftNavDocuments = processSearchResponseForLeftNavDocuments(searchResponse);

        if (!leftNavDocuments.isEmpty()) {
            deleteLeftNavDocuments(leftNavDocuments);
        }
    }

    @SneakyThrows
    private SearchResponse buildLeftNavSearchRequest(String categoryId) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.query(QueryBuilders.termQuery("categoryId", categoryId));
        SearchRequest searchRequest = new SearchRequest(navigationServiceConfig.getElasticSearchConfig().getLeftNavIndex().getName());
        searchRequest.source(searchSourceBuilder);
        return restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
    }

    private List<LeftNavDocument> processSearchResponseForLeftNavDocuments(final SearchResponse searchResponse) {
        List<LeftNavDocument> leftNavDocuments = new ArrayList<>();
        for (SearchHit searchHit : searchResponse.getHits()) {
            String searchHitAsString = searchHit.getSourceAsString();
            if (!searchHitAsString.isEmpty()) {
                try {
                    leftNavDocuments.add(objectMapper.readValue(searchHitAsString, LeftNavDocument.class));
                } catch (IOException e) {
                    log.error(", event_type=\"REPOSITORY\", action=\"PARSE_LEFTNAV_DOCUMENT\", Document={} ExceptionTrace=", searchHit.getId(), e);
                }
            }
        }
        return leftNavDocuments;
    }

    @LoggableEvent(eventType = Constants.REPOSITORY, action = Constants.GET_LEFTNAV_DOCUMENT_FROM_ES)
    private void deleteLeftNavDocuments(List<LeftNavDocument> leftNavDocuments) {
        BulkRequest bulkRequest = new BulkRequest();
        StringBuilder leftNavIdBuilder = new StringBuilder();
        for (LeftNavDocument leftNavDocument : leftNavDocuments) {
            DeleteRequest deleteRequest = new DeleteRequest(navigationServiceConfig.getElasticSearchConfig().getLeftNavIndex().getName(),
                    navigationServiceConfig.getElasticSearchConfig().getLeftNavIndex().getDocumentType6(),
                    leftNavDocument.getId());
            bulkRequest.add(deleteRequest);
        }
        String leftNavIdCombined = leftNavIdBuilder.toString();
        Instant start = Instant.now();
        asyncDelete(bulkRequest, leftNavIdCombined, start);
    }

    private void asyncDelete(BulkRequest bulkRequest, String leftNavIdCombined, Instant start) {
        try {
            BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            handleOnResponse(bulkResponse, contextMap, start, leftNavIdCombined);
        } catch(Exception e){
            log.error(CONTENT_SYNC_LOG_MAKER, FAILURE_LOG_FORMAT, Duration.between(start, Instant.now()).toMillis(), leftNavIdCombined, e.getMessage());
            log.error(REPOSITORY + ELASTICSEARCH_ASYNC_DELETE_LEFTNAV_DOCUMENT + "\" Error updating document", e);
        }

    }

    private void handleOnResponse(BulkResponse bulkItemResponses, Map<String, String> contextMap, Instant start, String leftNavIds) {
        contextMap.put(MDC_DESTINATION_PROPERTY, "ES:" + navigationServiceConfig.getElasticSearchConfig().getLeftNavIndex().getName());
        MDC.setContextMap(contextMap);
        Instant end = Instant.now();
        if (bulkItemResponses.hasFailures()) {
            log.error(CONTENT_SYNC_LOG_MAKER, FAILURE_LOG_FORMAT, Duration.between(start, end).toMillis(), leftNavIds, bulkItemResponses.buildFailureMessage());
            log.error(REPOSITORY + ELASTICSEARCH_ASYNC_DELETE_LEFTNAV_DOCUMENT + "\" ", bulkItemResponses.buildFailureMessage());
        } else {
            log.info(CONTENT_SYNC_LOG_MAKER, SUCCESS_LOG_FORMAT, Duration.between(start, end).toMillis(), leftNavIds);
        }
    }

}

package com.sixthday.store.repository;

import static com.sixthday.store.config.Constants.Actions.GET_STORE;
import static com.sixthday.store.config.Constants.Actions.STORE_SEARCH;
import static com.sixthday.store.config.Constants.Events.REPOSITORY_EVENT;
import static com.sixthday.store.config.Constants.Filters.IS_DISPLAYABLE;


import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.elasticsearch.action.search.SearchRequest;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.logger.logging.LoggableEvent;
import com.sixthday.store.config.StoreDetailsConfig;
import com.sixthday.store.exceptions.DocumentNotFoundException;
import com.sixthday.store.models.storeindex.StoreDocument;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryDocument;
import com.sixthday.store.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class ElasticsearchRepository  {
    private static final String ACTION = "action=\"";
    private static final String TERMS_FILTER_STORE_NUMBER = "storeNumber";
    private static final String TERMS_FILTER_STORE_ID = "id";

    private final StoreDetailsConfig config;
    private final RestHighLevelClient client;
    private ObjectMapper mapper;

    @Autowired
    public ElasticsearchRepository(final StoreDetailsConfig config, final RestHighLevelClient client) {
        this.config = config;
        this.client = client;
        this.mapper = new ObjectMapper().enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL).setTimeZone(TimeZone.getTimeZone("America/Chicago"));
    }

    @LoggableEvent(eventType = REPOSITORY_EVENT, action = STORE_SEARCH)
    public List<StoreDocument> filterStoresForStoreNumbers(List<String> storeNumbersToSearch, String filterFlag) {
        SearchResponse storesResponse = null;
        try {
            storesResponse = client.search(buildSearchRequestToFilterStoresForStoreNumbers(storeNumbersToSearch, filterFlag),RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error( "Filter Stores failed "  + e.getMessage());
        }

        List<StoreDocument> storeDocuments = processResponse(storesResponse);

        List<String> storeNumbersInElasticSearch = storeDocuments
                .stream().map(StoreDocument::getStoreNumber)
                .collect(Collectors.toList());

        logMissingStoresInElasticsearch(
                CollectionUtils.disjuntion(storeNumbersToSearch, storeNumbersInElasticSearch)
        );

        return storeDocuments;
    }

    private SearchRequest buildSearchRequestToFilterStoresForStoreNumbers(List<String> storeNumbersToSearch, String filterFlag ){

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery(filterFlag, true))
                .must(QueryBuilders.termQuery(IS_DISPLAYABLE, true))
                .must(QueryBuilders.termsQuery(TERMS_FILTER_STORE_NUMBER, storeNumbersToSearch.toArray())));
        SearchRequest searchRequest = new SearchRequest(config.getElasticSearchConfig().getStoreIndexName());
        searchRequest.source(searchSourceBuilder);
        return searchRequest;

    }

    @LoggableEvent(eventType = REPOSITORY_EVENT, action = GET_STORE)
    public List<StoreDocument> getStoresById(String storeId) {
        SearchResponse storesResponse = null;
        try {
            storesResponse = client.search(buildSearchRequestById(storeId), RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(" Search Request By Id failed " + storeId + " : " + e.getMessage());
        }

        List<StoreDocument> storeDocuments = processResponse(storesResponse);

        if (storeDocuments.isEmpty()) {
            throw new DocumentNotFoundException("Store is not available in Elasticsearch with storeId:" + storeId);
        }
        return storeDocuments;
    }

    public SearchRequest buildSearchRequestById (String storeId) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.boolQuery().must(QueryBuilders.queryStringQuery(TERMS_FILTER_STORE_ID + ":\"" + storeId + "\""))
                .must(QueryBuilders.termQuery(IS_DISPLAYABLE, true)));
        SearchRequest searchRequest = new SearchRequest(config.getElasticSearchConfig().getStoreIndexName());
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }

    public SearchRequest buildSearchRequestForAllStoresSkus (List <String > storeNumbers, String skuId) {

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.termsQuery(TERMS_FILTER_STORE_NUMBER, storeNumbers.toArray()))
                .must(QueryBuilders.termQuery("skuId", skuId));

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQuery);
        SearchRequest searchRequest = new SearchRequest(config.getElasticSearchConfig().getStoreSkuInventoryIndexName());
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }

    public List<StoreSkuInventoryDocument> getAllStoresSkus(List<String> storeNumbers, String skuId) {
        SearchResponse skuStoresResponse = null;
        try {
            skuStoresResponse = client.search(buildSearchRequestForAllStoresSkus ( storeNumbers,  skuId), RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.debug("Get Sku Search Request: {}", skuId + ": " + e.getMessage());
        }
        return processSkuDocumentResponse(skuStoresResponse);
    }

    private List<StoreSkuInventoryDocument> processSkuDocumentResponse(SearchResponse skuStoresResponse) {
        SearchHit[] searchHits = skuStoresResponse.getHits().getHits();
        return Arrays.stream(searchHits)
                .map(this::mapSearchHitToSkuStoreDocument)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private StoreSkuInventoryDocument mapSearchHitToSkuStoreDocument(SearchHit searchHit) {
        try {
            return mapper.readValue(searchHit.getSourceAsString(), StoreSkuInventoryDocument.class);
        } catch (IOException e) {
            log.error(ACTION + STORE_SEARCH + "\" Error occurred while converting response to StoreSkuInventoryDocument", e);
            log.info("store_response={}", searchHit.getSourceAsString());
            return null;
        }
    }

    private void logMissingStoresInElasticsearch(List<String> storeNumbersToLog) {
        if (!storeNumbersToLog.isEmpty())
            log.info("Stores are not available in Elasticsearch with numbers: {}", storeNumbersToLog);
    }

    private List<StoreDocument> processResponse(SearchResponse storesResponse) {
        SearchHit[] searchHits = storesResponse.getHits().getHits();
        return Arrays.stream(searchHits)
                .map(this::mapSearchHitToStoreDocument)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private StoreDocument mapSearchHitToStoreDocument(SearchHit searchHit) {
        try {
            return mapper.readValue(searchHit.getSourceAsString(), StoreDocument.class);
        } catch (IOException e) {
            log.error(ACTION + STORE_SEARCH + "\" Error occurred while converting response to StoreDocument", e);
            log.info("store_response={}", searchHit.getSourceAsString());
            return null;
        }
    }
}

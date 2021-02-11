package com.sixthday.navigation.api.elasticsearch.repository;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.logger.logging.LoggableEvent;
import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.exceptions.CategoryNotFoundException;
import com.sixthday.navigation.config.Constants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;

import static com.sixthday.navigation.config.Constants.*;
import static org.elasticsearch.index.query.QueryBuilders.*;

@Repository
@Slf4j
public class ESCategoryDocumentRepository {

    private NavigationServiceConfig navigationServiceConfig;
    private ObjectMapper objectMapper;
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    public ESCategoryDocumentRepository(final RestHighLevelClient restHighLevelClient,
                                        final NavigationServiceConfig navigationServiceConfig) {
        this.restHighLevelClient = restHighLevelClient;
        this.navigationServiceConfig = navigationServiceConfig;
        this.objectMapper = new ObjectMapper().enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL).setTimeZone(TimeZone.getTimeZone("America/Chicago"));
    }

    @PreDestroy
    @SneakyThrows
    public void closeClient() {
        restHighLevelClient.close();
    }

    @LoggableEvent(eventType = Constants.REPOSITORY, action = GET_CATEGORY_DOCUMENT_FROM_ES)
    public CategoryDocument getCategoryDocument(final String categoryId) {
        GetResponse getResponse = buildSearchRequest(categoryId);
        if (getResponse.isExists()) {
            return processSearchResponseForCategory(getResponse);
        } else {
            throw new CategoryNotFoundException(categoryId);
        }
    }

    @LoggableEvent(eventType = Constants.REPOSITORY, action = Constants.GET_CATEGORY_DOCUMENTS_FROM_ES)
    public List<CategoryDocument> getCategoryDocuments(final List<String> categoryIds) {
        SearchResponse searchResponse = buildSearchRequest(categoryIds);
        List<CategoryDocument> categoryDocuments = processSearchResponseForCategories(searchResponse);
        if (categoryDocuments.isEmpty()) {
            throw new CategoryNotFoundException(categoryIds.toString());
        }
        return categoryDocuments;
    }

    @SneakyThrows
    private SearchResponse buildSearchRequest(final List<String> categoryIds) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
            .filter(boolQuery()
                .must(QueryBuilders.idsQuery().addIds(categoryIds.toArray(new String[0])))
                .mustNot(termQuery("isDeleted", true)));
        for (int i = 0; i < categoryIds.size(); i++) {
            queryBuilder.should(termQuery("id", categoryIds.get(i)).boost(i));
        }
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            .query(queryBuilder)
            .sort(SortBuilders.scoreSort().order(SortOrder.ASC));
        SearchRequest searchRequest = new SearchRequest(navigationServiceConfig.getElasticSearchConfig().getIndexName())
            .types(navigationServiceConfig.getElasticSearchConfig().getDocumentType6())
            .source(searchSourceBuilder);
        return restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
    }

    @SneakyThrows
    private GetResponse buildSearchRequest(final String categoryId) {
        return restHighLevelClient.get(new GetRequest(navigationServiceConfig.getElasticSearchConfig().getIndexName(),
                navigationServiceConfig.getElasticSearchConfig().getDocumentType6(),
                categoryId), RequestOptions.DEFAULT);
    }

    private List<CategoryDocument> processSearchResponseForCategories(final SearchResponse searchResponse) {
        List<CategoryDocument> categoryDocuments = new ArrayList<>();
        for (SearchHit searchHit : searchResponse.getHits()) {
            String searchHitAsString = searchHit.getSourceAsString();
            if (!searchHitAsString.isEmpty()) {
                try {
                    categoryDocuments.add(objectMapper.readValue(searchHitAsString, CategoryDocument.class));
                } catch (IOException e) {
                    log.error(ACTION + PARSE_CATEGORY_DOCUMENT + "\" Error parsing document" + searchHit.getId(), e);
                    throw new CategoryNotFoundException(searchHit.getId());
                }
            }
        }
        return categoryDocuments;
    }

    private CategoryDocument processSearchResponseForCategory(final GetResponse getResponse) {
        try {
            return objectMapper.readValue(getResponse.getSourceAsString(), CategoryDocument.class);
        } catch (IOException e) {
            log.error(ACTION + PARSE_CATEGORY_DOCUMENT + "\" Error parsing document" + getResponse.getId(), e);
            throw new CategoryNotFoundException(getResponse.getId());
        }
    }
}

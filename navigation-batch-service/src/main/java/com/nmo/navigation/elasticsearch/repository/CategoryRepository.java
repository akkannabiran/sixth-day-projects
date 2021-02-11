package com.sixthday.navigation.elasticsearch.repository;

import com.sixthday.navigation.batch.vo.CategoryDocuments;
import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import com.sixthday.navigation.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.exceptions.NavigationBatchServiceException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static com.sixthday.sixthdayLogging.OperationType.*;
import static com.sixthday.sixthdayLogging.logDebugOperation;
import static org.elasticsearch.index.query.QueryBuilders.*;

@Repository
@Slf4j
public class CategoryRepository {

    private static final String IS_DELETED = "isDeleted";
    private NavigationBatchServiceConfig navigationBatchServiceConfig;
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    public CategoryRepository(final RestHighLevelClient restHighLevelClient, final NavigationBatchServiceConfig navigationBatchServiceConfig) {
        this.restHighLevelClient = restHighLevelClient;
        this.navigationBatchServiceConfig = navigationBatchServiceConfig;
    }

    @PreDestroy
    @SneakyThrows
    public void closeClient() {
        restHighLevelClient.close();
    }

    public CategoryDocument getCategoryDocument(final String categoryId) {
        return logDebugOperation(log, null, ELASTICSEARCH_GET_CATEGORY_DOCUMENT, () -> {
            SearchResponse searchResponse = null;
            try {
                searchResponse = restHighLevelClient.search(buildSearchRequest(categoryId), RequestOptions.DEFAULT);
            } catch (IOException e) {
                log.error("Error while retrieving category document from ES", e);
            }
            return processSearchResponseForCategory(searchResponse);
        });
    }

    private SearchRequest buildSearchRequest(final String categoryId) {
        return new SearchRequest()
                .indices(navigationBatchServiceConfig.getElasticSearchConfig().getCategoryIndex().getName())
                .source(new SearchSourceBuilder()
                        .query(QueryBuilders.boolQuery().filter(boolQuery()
                                .must(termQuery("_id", categoryId))
                                .minimumShouldMatch(1)
                                .should(termQuery(IS_DELETED, false))
                                .should(boolQuery()
                                        .mustNot(existsQuery(IS_DELETED))))));
    }

    @SneakyThrows
    private CategoryDocument processSearchResponseForCategory(final SearchResponse searchResponse) {
        SearchHits searchHits = searchResponse.getHits();
        for (SearchHit hit : searchHits.getHits()) {
            if (hit.hasSource()) {
                return navigationBatchServiceConfig.elasticSearchObjectMapper().readValue(hit.getSourceAsString(), CategoryDocument.class);
            }
        }
        return null;
    }

    public CategoryDocuments getCategoryDocuments(@NotNull final String scrollId) {
        return logDebugOperation(log, null, ELASTICSEARCH_GET_CATEGORY_DOCUMENTS_BY_SCROLL, () -> {
            List<CategoryDocument> categoryDocumentsChunk;
            try {
                SearchResponse searchResponse = restHighLevelClient.scroll(new SearchScrollRequest(scrollId)
                        .scroll(new TimeValue(navigationBatchServiceConfig.getLeftNavBatchConfig().getScrollTimeout())), RequestOptions.DEFAULT);
                categoryDocumentsChunk = processSearchResponseForCategoryRange(searchResponse);
                return new CategoryDocuments(searchResponse.getScrollId(), categoryDocumentsChunk);
            } catch (IOException e) {
                throw new NavigationBatchServiceException("Error occurred while loading category documents", e);
            }
        });
    }

    public CategoryDocuments getCategoryDocuments(int numberOfDocuments) {
        return logDebugOperation(log, null, ELASTICSEARCH_GET_CATEGORY_DOCUMENTS, () -> {
            SearchResponse searchResponse = null;
            try {
                searchResponse = restHighLevelClient.search(buildSearchRequestWithChildrenNotExists(numberOfDocuments), RequestOptions.DEFAULT);
            } catch (IOException e) {
                log.error("Error while retrieving category documents from ES", e);
            }
            List<CategoryDocument> categoryDocumentsChunk = processSearchResponseForCategoryRange(searchResponse);
            return new CategoryDocuments(searchResponse.getScrollId(), categoryDocumentsChunk);
        });
    }

    private SearchRequest buildSearchRequestWithChildrenNotExists(int numberOfDocuments) {
        return new SearchRequest(navigationBatchServiceConfig.getElasticSearchConfig().getCategoryIndex().getName())
                .source(new SearchSourceBuilder()
                        .query(boolQuery().filter(boolQuery()
                                .must(termQuery("hidden", false))
                                .must(termQuery("noResults", false))
                                .minimumShouldMatch(1)
                                .should(termQuery(IS_DELETED, false))
                                .should(boolQuery()
                                        .mustNot(existsQuery(IS_DELETED))))).size(numberOfDocuments))
                .scroll(new TimeValue(navigationBatchServiceConfig.getLeftNavBatchConfig().getScrollTimeout()));
    }

    @SneakyThrows
    private List<CategoryDocument> processSearchResponseForCategoryRange(final SearchResponse searchResponse) {
        List<CategoryDocument> categoryDocuments = new LinkedList<>();
        SearchHits searchHits = searchResponse.getHits();
        for (SearchHit hit : searchHits.getHits()) {
            if (hit.hasSource()) {
                categoryDocuments.add(navigationBatchServiceConfig.elasticSearchObjectMapper().readValue(BytesReference.toBytes(hit.getSourceRef()), CategoryDocument.class));
            }
        }
        return categoryDocuments;
    }
}

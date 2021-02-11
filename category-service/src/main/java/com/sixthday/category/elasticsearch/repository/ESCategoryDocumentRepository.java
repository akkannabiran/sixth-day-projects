package com.sixthday.category.elasticsearch.repository;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.sixthday.category.elasticsearch.config.ElasticSearchConfig;
import com.sixthday.category.exceptions.CategoryDocumentNotFoundException;
import com.sixthday.logger.logging.LoggableEvent;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScriptSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;

import static com.sixthday.logger.logging.LoggingAction.GET_CATEGORY_DOCUMENTS_FROM_ES;
import static com.sixthday.logger.logging.LoggingAction.GET_CATEGORY_DOCUMENT_FROM_ES;
import static com.sixthday.logger.logging.LoggingEvent.REPOSITORY;

@Repository
@Slf4j
public class ESCategoryDocumentRepository {

    private static final String CATEGORY_ELASTIC_SEARCH = "category-elastic-search";

    @Qualifier(value = "CategoryTransportClient")
    private RestHighLevelClient restHighLevelClient;
    private ElasticSearchConfig elasticSearchConfig;
    private ObjectMapper objectMapper;

    @Autowired
    public ESCategoryDocumentRepository(final ElasticSearchConfig elasticSearchConfig, @Qualifier(value = "CategoryHighLevelClient") RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
        this.elasticSearchConfig = elasticSearchConfig;
        this.objectMapper = new ObjectMapper().enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL).setTimeZone(TimeZone.getTimeZone("America/Chicago"));
    }

    @PreDestroy
    public void closeClient() throws IOException{
        restHighLevelClient.close();
    }

    @LoggableEvent(eventType = REPOSITORY, action = GET_CATEGORY_DOCUMENTS_FROM_ES, hystrixCommandKey = CATEGORY_ELASTIC_SEARCH)
    @HystrixCommand(groupKey = CATEGORY_ELASTIC_SEARCH, commandKey = CATEGORY_ELASTIC_SEARCH, threadPoolKey = CATEGORY_ELASTIC_SEARCH)
    public List<CategoryDocument> getCategoryDocuments(final Set<String> categoryIds) {
        try{
            String code = "for ( int i = 0; i < params.ids.size(); i++) {if (params['_source']['id'] == params.ids[i]) { return (params.ids.size() + i);} }";
            Map<String, Object> params = new HashMap<>();
            params.put("ids", categoryIds);
            SearchSourceBuilder categoryDocumentsSearchSourceBuilder = new SearchSourceBuilder()
                    .size(categoryIds.size())
                    .query(QueryBuilders.idsQuery().addIds(categoryIds.toArray(new String[0])))
                    .sort(SortBuilders.scriptSort(new Script(ScriptType.INLINE, "painless", code, params), ScriptSortBuilder.ScriptSortType.NUMBER));
            SearchRequest categoryDocumentsSearchRequest = new SearchRequest(elasticSearchConfig.getCategoryConfig().getIndexName())
                    .types(CategoryDocument.DOCUMENT_TYPE)
                    .source(categoryDocumentsSearchSourceBuilder);
            SearchResponse categoryDocumentsResponse = restHighLevelClient.search(categoryDocumentsSearchRequest, RequestOptions.DEFAULT);
            return processSearchResponseForCategories(categoryDocumentsResponse);
        } catch (Exception ioe) {
            log.error("Exception occurred in category repository while retrieving categories from ES:", ioe);
            throw new CategoryDocumentNotFoundException(ioe.getMessage());
        }
    }

    private List<CategoryDocument> processSearchResponseForCategories(final SearchResponse searchResponse) {
        List<CategoryDocument> categoryDocuments = new ArrayList<>();
        for (SearchHit searchHit : searchResponse.getHits()) {
            String categoryDocumentString = searchHit.getSourceAsString();
            if (!categoryDocumentString.isEmpty()) {
                try {
                    categoryDocuments.add(objectMapper.readValue(categoryDocumentString, CategoryDocument.class));
                } catch (IOException e) {
                    log.error("Error parsing document " + searchHit.getId(), e);
                }
            }
        }
        return categoryDocuments;
    }

    @LoggableEvent(eventType = REPOSITORY, action = GET_CATEGORY_DOCUMENT_FROM_ES, hystrixCommandKey = CATEGORY_ELASTIC_SEARCH)
    @HystrixCommand(groupKey = CATEGORY_ELASTIC_SEARCH, commandKey = CATEGORY_ELASTIC_SEARCH, threadPoolKey = CATEGORY_ELASTIC_SEARCH)
    public CategoryDocument getCategoryDocument(final String categoryId) {
        GetResponse categoryResponse;
        try {
            categoryResponse = restHighLevelClient.get(buildCategoryGetRequest(categoryId), RequestOptions.DEFAULT);
        } catch (IOException io) {
            log.error("Error occurred while getting category document with id = \"{}\" from index", io);
            throw new CategoryDocumentNotFoundException(io.getMessage());
        }
        return processGetResponseForCategory(categoryResponse);
    }

    private GetRequest buildCategoryGetRequest(final String categoryId) {
        return new GetRequest(
                elasticSearchConfig.getCategoryConfig().getIndexName(),
                elasticSearchConfig.getCategoryConfig().getDocumentType(),
                categoryId);
    }
    private CategoryDocument processGetResponseForCategory(final GetResponse categoryResponse) {
        if (categoryResponse != null && categoryResponse.isExists()) {
            String source = categoryResponse.getSourceAsString();
            if (StringUtils.isNotEmpty(source)) {
                try {
                    return objectMapper.readValue(source, CategoryDocument.class);
                } catch (IOException ex) {
                    log.error("Error occurred parsing product document response: {}", source, ex);
                }
            }
        }
        return null;
    }
}

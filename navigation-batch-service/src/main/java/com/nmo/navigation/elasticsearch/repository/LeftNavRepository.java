package com.sixthday.navigation.elasticsearch.repository;

import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import com.sixthday.navigation.elasticsearch.documents.LeftNavDocument;
import com.sixthday.navigation.exceptions.NavigationBatchServiceException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.util.*;

import static com.sixthday.sixthdayLogging.OperationType.*;
import static com.sixthday.sixthdayLogging.logDebugOperation;

@Repository
@Slf4j
public class LeftNavRepository {
    private NavigationBatchServiceConfig navigationBatchServiceConfig;
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    public LeftNavRepository(final RestHighLevelClient restHighLevelClient, final NavigationBatchServiceConfig navigationBatchServiceConfig) {
        this.restHighLevelClient = restHighLevelClient;
        this.navigationBatchServiceConfig = navigationBatchServiceConfig;
    }

    @PreDestroy
    @SneakyThrows
    public void closeClient() {
        restHighLevelClient.close();
    }

    public boolean save(List<LeftNavDocument> leftNavDocuments) {
        return logDebugOperation(log, null, ELASTICSEARCH_UPSERT_LEFTNAV_DOCUMENT, () -> {
            BulkResponse response = buildSaveRequest(leftNavDocuments);
            return !response.hasFailures();
        });
    }

    @SneakyThrows
    private BulkResponse buildSaveRequest(List<LeftNavDocument> leftNavDocuments) {
        BulkRequest bulkRequest = new BulkRequest();
        for (LeftNavDocument leftNavDocument : leftNavDocuments) {
            bulkRequest.add(new UpdateRequest()
                    .index(navigationBatchServiceConfig.getElasticSearchConfig().getLeftNavIndex().getName())
                    .type(navigationBatchServiceConfig.getElasticSearchConfig().getLeftNavIndex().getDocumentType6())
                    .id(leftNavDocument.getId())
                    .doc(navigationBatchServiceConfig.elasticSearchObjectMapper().writeValueAsString(leftNavDocument), XContentType.JSON)
                    .docAsUpsert(true));
        }
        return restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    public Set<String> getPathsByReferenceId(String categoryId) {
        return logDebugOperation(log, null, ELASTICSEARCH_GET_PATHS_BY_REFERENCEID, () -> {
            SearchResponse searchResponse = buildSearchRequestWithReferenceIdMatches(categoryId);
            Set<String> paths = new HashSet<>();
            searchResponse.getHits().forEach(hit ->
                    paths.add(hit.getId())
            );
            return paths;
        });
    }

    @SneakyThrows
    private SearchResponse buildSearchRequestWithReferenceIdMatches(String categoryId) {
        String[] includes = {"id"};
        String[] excludes = {};

        SearchRequest searchRequest = new SearchRequest()
                .indices(navigationBatchServiceConfig.getElasticSearchConfig().getLeftNavIndex().getName())
                .source(new SearchSourceBuilder()
                        .from(0)
                        .size(navigationBatchServiceConfig.getLeftNavBatchConfig().getMaxLeftnavTobeRebuild())
                        .query(QueryBuilders.matchQuery("referenceIds", categoryId))
                        .fetchSource(includes, excludes));

        return restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
    }

    public LeftNavDocument getLeftNavById(String path) {
        GetResponse getResponse = buildGetResponseById(path);
        if (getResponse != null && getResponse.isExists()) {
            try {
                return navigationBatchServiceConfig.elasticSearchObjectMapper().readValue(getResponse.getSourceAsString(), LeftNavDocument.class);
            } catch (Exception e) {
                throw new NavigationBatchServiceException("Unable to retrieve left nav documents from elasticsearch. Message from downstream: ", e);
            }
        }
        return null;
    }

    public Set<String> getCategorySiblingsPath(String path) {
        return logDebugOperation(log, null, GET_LEFTNAV_PATHS, () -> {
            Set<String> paths;
            LeftNavDocument leftNav = getLeftNavById(path);
            if (leftNav == null) {
                paths = new HashSet<>();
            } else {
                paths = getPathsFromLeftNav(leftNav);
            }
            return paths;
        });
    }

    private Set<String> getPathsFromLeftNav(LeftNavDocument leftNav) {
        Set<String> paths = new HashSet<>();
        if (null != leftNav.getLeftNav() && !leftNav.getLeftNav().isEmpty()) {
            leftNav.getLeftNav().forEach(leftTreeNode -> paths.add(leftTreeNode.getPath()));
        }
        return paths;
    }

    @SneakyThrows
    private GetResponse buildGetResponseById(final String path) {
        GetRequest getRequest = new GetRequest(
            navigationBatchServiceConfig.getElasticSearchConfig().getLeftNavIndex().getName(),
            navigationBatchServiceConfig.getElasticSearchConfig().getLeftNavIndex().getDocumentType6(),
            path);
        return restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
    }
}

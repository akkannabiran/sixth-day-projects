package com.sixthday.navigation.integration.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.elasticsearch.documents.LeftNavDocument;
import lombok.SneakyThrows;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({RestHighLevelClient.class})
public class LeftNavSyncRepositoryTest {

    @InjectMocks
    private LeftNavSyncRepository leftNavSyncRepository;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NavigationServiceConfig navigationServiceConfig;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private RestHighLevelClient restHighLevelClient;
    @Mock
    private SearchResponse searchResponse;
    @Mock
    private BulkResponse bulkItemResponse;

    @Test
    @SneakyThrows
    public void shouldCallCloseClientMethodAtLeastOnce() {
        leftNavSyncRepository.closeClient();
        verify(restHighLevelClient).close();
    }

    @Test
    @SneakyThrows
    public void shouldFetchAndDeleteLeftNavDocumentWhenNoFailure() {
        LeftNavDocument input = new LeftNavDocument();

        BytesReference source = new BytesArray(new ObjectMapper().writeValueAsString(input));
        SearchHit searchHit1 = new SearchHit(1);
        searchHit1.sourceRef(source);

        SearchHit searchHit2 = new SearchHit(1);
        searchHit2.sourceRef(source);

        SearchHits searchHits = new SearchHits(new SearchHit[]{searchHit1, searchHit2}, 5, 10);

        when(searchResponse.getHits()).thenReturn(searchHits);
        when(navigationServiceConfig.getElasticSearchConfig().getLeftNavIndex().getName()).thenReturn("index");
        when(restHighLevelClient.search(any(SearchRequest.class), any(RequestOptions.class))).thenReturn(searchResponse);
        when(objectMapper.readValue(anyString(), eq(LeftNavDocument.class))).thenReturn(input);
        when(restHighLevelClient.bulk(any(BulkRequest.class), any(RequestOptions.class))).thenReturn(bulkItemResponse);
        when(bulkItemResponse.hasFailures()).thenReturn(false);

        leftNavSyncRepository.fetchAndDeleteLeftNavDocument("cat1");

        verify(restHighLevelClient).search(any(SearchRequest.class), any(RequestOptions.class));
        verify(restHighLevelClient).bulk(any(BulkRequest.class), any(RequestOptions.class));
    }

    @Test
    @SneakyThrows
    public void shouldFetchAndDeleteLeftNavDocumentWhenFailure() {
        LeftNavDocument input = new LeftNavDocument();

        BytesReference source = new BytesArray(new ObjectMapper().writeValueAsString(input));
        SearchHit searchHit1 = new SearchHit(1);
        searchHit1.sourceRef(source);

        SearchHit searchHit2 = new SearchHit(1);
        searchHit2.sourceRef(source);

        SearchHits searchHits = new SearchHits(new SearchHit[]{searchHit1, searchHit2}, 5, 10);

        when(searchResponse.getHits()).thenReturn(searchHits);
        when(navigationServiceConfig.getElasticSearchConfig().getLeftNavIndex().getName()).thenReturn("index");
        when(restHighLevelClient.search(any(SearchRequest.class), any(RequestOptions.class))).thenReturn(searchResponse);
        when(objectMapper.readValue(anyString(), eq(LeftNavDocument.class))).thenReturn(input);
        when(restHighLevelClient.bulk(any(BulkRequest.class), any(RequestOptions.class))).thenReturn(bulkItemResponse);
        when(bulkItemResponse.hasFailures()).thenReturn(true);

        leftNavSyncRepository.fetchAndDeleteLeftNavDocument("cat1");

        verify(restHighLevelClient).search(any(SearchRequest.class), any(RequestOptions.class));
        verify(restHighLevelClient).bulk(any(BulkRequest.class), any(RequestOptions.class));
    }

    @Test
    @SneakyThrows
    public void shouldFetchAndDeleteLeftNavDocumentHadAnIssue() {
        LeftNavDocument input = new LeftNavDocument();

        BytesReference source = new BytesArray(new ObjectMapper().writeValueAsString(input));
        SearchHit searchHit1 = new SearchHit(1);
        searchHit1.sourceRef(source);

        SearchHit searchHit2 = new SearchHit(1);
        searchHit2.sourceRef(source);

        SearchHits searchHits = new SearchHits(new SearchHit[]{searchHit1, searchHit2}, 5, 10);

        when(searchResponse.getHits()).thenReturn(searchHits);
        when(navigationServiceConfig.getElasticSearchConfig().getLeftNavIndex().getName()).thenReturn("index");
        when(restHighLevelClient.search(any(SearchRequest.class), any(RequestOptions.class))).thenReturn(searchResponse);
        when(objectMapper.readValue(anyString(), eq(LeftNavDocument.class))).thenReturn(input);
        when(restHighLevelClient.bulk(any(BulkRequest.class), any(RequestOptions.class))).thenThrow(Exception.class);
        when(bulkItemResponse.hasFailures()).thenReturn(true);

        leftNavSyncRepository.fetchAndDeleteLeftNavDocument("cat1");

        verify(restHighLevelClient).search(any(SearchRequest.class), any(RequestOptions.class));
        verify(restHighLevelClient).bulk(any(BulkRequest.class), any(RequestOptions.class));
        verify(bulkItemResponse, never()).hasFailures();
    }

    @Test
    @SneakyThrows
    public void testDontCallRestClientWhenNoLeftNavFound() {
        when(searchResponse.getHits()).thenReturn(new SearchHits(new SearchHit[0], 0, 0));
        when(navigationServiceConfig.getElasticSearchConfig().getLeftNavIndex().getName()).thenReturn("index");
        when(restHighLevelClient.search(any(SearchRequest.class), any(RequestOptions.class))).thenReturn(searchResponse);
        when(restHighLevelClient.bulk(any(BulkRequest.class), any(RequestOptions.class))).thenThrow(Exception.class);
        when(bulkItemResponse.hasFailures()).thenReturn(true);

        leftNavSyncRepository.fetchAndDeleteLeftNavDocument("cat1");

        verify(restHighLevelClient).search(any(SearchRequest.class), any(RequestOptions.class));
        verify(restHighLevelClient, never()).bulk(any(BulkRequest.class), any(RequestOptions.class));
        verify(bulkItemResponse, never()).hasFailures();
    }

    @Test
    @SneakyThrows
    public void testDontCallRestClientWhenNoValidLeftNavFound() {
        LeftNavDocument input = new LeftNavDocument();

        BytesReference source = new BytesArray(new ObjectMapper().writeValueAsString(input));
        SearchHit searchHit1 = new SearchHit(1);
        searchHit1.sourceRef(source);

        SearchHit searchHit2 = new SearchHit(1);
        searchHit2.sourceRef(source);

        SearchHits searchHits = new SearchHits(new SearchHit[]{searchHit1, searchHit2}, 5, 10);

        when(searchResponse.getHits()).thenReturn(searchHits);
        when(navigationServiceConfig.getElasticSearchConfig().getLeftNavIndex().getName()).thenReturn("index");
        when(restHighLevelClient.search(any(SearchRequest.class), any(RequestOptions.class))).thenReturn(searchResponse);
        when(objectMapper.readValue(anyString(), eq(LeftNavDocument.class))).thenThrow(IOException.class);
        when(restHighLevelClient.bulk(any(BulkRequest.class), any(RequestOptions.class))).thenThrow(Exception.class);
        when(bulkItemResponse.hasFailures()).thenReturn(true);

        leftNavSyncRepository.fetchAndDeleteLeftNavDocument("cat1");

        verify(restHighLevelClient).search(any(SearchRequest.class), any(RequestOptions.class));
        verify(restHighLevelClient, never()).bulk(any(BulkRequest.class), any(RequestOptions.class));
        verify(bulkItemResponse, never()).hasFailures();
    }
}

package com.sixthday.navigation.batch.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.navigation.batch.vo.LeftNavTreeNode;
import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import com.sixthday.navigation.elasticsearch.documents.LeftNavDocument;
import com.sixthday.navigation.elasticsearch.repository.LeftNavRepository;
import com.sixthday.navigation.exceptions.NavigationBatchServiceException;
import lombok.SneakyThrows;
import org.elasticsearch.action.bulk.*;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.*;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({XContentBuilder.class, XContentFactory.class, BulkProcessor.class, RestHighLevelClient.class})
public class LeftNavRepositoryTest {
    @InjectMocks
    private LeftNavRepository leftNavRepository;
    @Mock
    private SearchResponse searchResponse;
    @Mock
    private GetResponse getResponse;
    @Mock
    private BulkResponse bulkResponse;
    @Mock
    private RestHighLevelClient restHighLevelClient;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NavigationBatchServiceConfig navigationBatchServiceConfig;

    @Test
    public void shouldCallCloseClient() throws IOException {
        leftNavRepository.closeClient();
        verify(restHighLevelClient).close();
    }

    @SneakyThrows
    @Test(expected = NavigationBatchServiceException.class)
    public void shouldThrowExceptionWhenGetCategorySiblingsPathCalled() {
        LeftNavDocument leftNavDocument = new LeftNavDocument();

        when(navigationBatchServiceConfig.getElasticSearchConfig().getLeftNavIndex().getName()).thenReturn("some_index");
        when(navigationBatchServiceConfig.getElasticSearchConfig().getLeftNavIndex().getDocumentType6()).thenReturn("some_type");
        when(restHighLevelClient.get(any(GetRequest.class), any(RequestOptions.class))).thenReturn(getResponse);
        when(getResponse.isExists()).thenReturn(true);
        when(getResponse.getSourceAsString()).thenReturn(new ObjectMapper().writeValueAsString(leftNavDocument));
        when(navigationBatchServiceConfig.elasticSearchObjectMapper()).thenThrow(IOException.class);

        leftNavRepository.getCategorySiblingsPath("catId2");
    }

    @SneakyThrows
    @Test
    public void shouldReturnPathsWhenGetCategorySiblingsPathCalled() {
        LeftNavDocument leftNavDocument = new LeftNavDocument();
        LeftNavTreeNode leftNavTreeNode = new LeftNavTreeNode();
        leftNavDocument.setLeftNav(Collections.singletonList(leftNavTreeNode));

        when(restHighLevelClient.get(any(GetRequest.class), any(RequestOptions.class))).thenReturn(getResponse);
        when(getResponse.isExists()).thenReturn(true);
        when(getResponse.getSourceAsString()).thenReturn(new ObjectMapper().writeValueAsString(leftNavDocument));
        when(navigationBatchServiceConfig.elasticSearchObjectMapper().readValue(any(String.class), eq(LeftNavDocument.class))).thenReturn(leftNavDocument);

        Set<String> paths = leftNavRepository.getCategorySiblingsPath("catId2");

        assertNotNull(paths);
        assertThat(paths.size(), is(1));
    }

    @SneakyThrows
    @Test
    public void shouldReturnEmtpyPathsWhenGetCategorySiblingsPathCalled() {
        LeftNavDocument leftNavDocument = new LeftNavDocument();

        when(restHighLevelClient.get(any(GetRequest.class), any(RequestOptions.class))).thenReturn(getResponse);
        when(getResponse.isExists()).thenReturn(true);
        when(getResponse.getSourceAsString()).thenReturn(new ObjectMapper().writeValueAsString(leftNavDocument));

        when(navigationBatchServiceConfig.elasticSearchObjectMapper().readValue(any(String.class), eq(LeftNavDocument.class))).thenReturn(leftNavDocument);

        Set<String> paths = leftNavRepository.getCategorySiblingsPath("catId2");

        assertNotNull(paths);
        assertThat(paths.size(), is(0));
    }

    @SneakyThrows
    @Test
    public void shouldReturnEmptyPathsWhenGetCategorySiblingsPathCalledForNonExistingPath() {

        when(restHighLevelClient.get(any(GetRequest.class), any(RequestOptions.class))).thenReturn(getResponse);
        when(getResponse.isExists()).thenReturn(false);

        Set<String> paths = leftNavRepository.getCategorySiblingsPath("NonExistingPathId");

        assertNotNull(paths);
        assertThat(paths.size(), is(0));
    }

    @SneakyThrows
    @Test
    public void shouldReturnEmptyPathsWhenESReturnsNullGetResponse() {

        when(restHighLevelClient.get(any(GetRequest.class), any(RequestOptions.class))).thenReturn(null);

        Set<String> paths = leftNavRepository.getCategorySiblingsPath("NonExistingPathId");

        assertNotNull(paths);
        assertThat(paths.size(), is(0));
    }

    @Test
    @SneakyThrows
    public void shouldReturnEmptyWhenGetPathsByReferenceId() {
        LeftNavDocument leftNavDocument = new LeftNavDocument();

        BytesReference source = new BytesArray(new ObjectMapper().writeValueAsString(leftNavDocument));
        SearchHit searchHit1 = new SearchHit(1);
        searchHit1.sourceRef(source);

        SearchHit searchHit2 = new SearchHit(1);
        searchHit2.sourceRef(source);

        SearchHits searchHits = new SearchHits(new SearchHit[]{searchHit1, searchHit2}, 5, 10);

        when(searchResponse.getHits()).thenReturn(searchHits);
        when(navigationBatchServiceConfig.getElasticSearchConfig().getLeftNavIndex().getName()).thenReturn("some_index");
        when(restHighLevelClient.search(any(SearchRequest.class), any(RequestOptions.class))).thenReturn(searchResponse);

        assertThat(!leftNavRepository.getPathsByReferenceId("catId2").isEmpty(), is(true));
    }

    @SneakyThrows
    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionSaveLeftNavDocumentToElasticSearch() {
        List<LeftNavDocument> leftNavDocuments = new ArrayList<>();
        when(restHighLevelClient.bulk(any(BulkRequest.class), any(RequestOptions.class))).thenThrow(new RuntimeException());
        assertThat(leftNavRepository.save(leftNavDocuments), is(true));
    }

    @Test
    @SneakyThrows
    public void shouldSaveLeftNavDocumentToElasticSearch() {
        List<LeftNavDocument> leftNavDocuments = new ArrayList<>();
        when(restHighLevelClient.bulk(any(BulkRequest.class), any(RequestOptions.class))).thenReturn(bulkResponse);
        assertThat(leftNavRepository.save(leftNavDocuments), is(true));
    }
}

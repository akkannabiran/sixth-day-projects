package com.sixthday.navigation.api.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.repository.ESCategoryDocumentRepository;
import com.sixthday.navigation.api.exceptions.CategoryNotFoundException;
import lombok.SneakyThrows;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
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

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RestHighLevelClient.class, NavigationServiceConfig.class})
@PowerMockIgnore("javax.management.*")
public class ESCategoryDocumentRepositoryTest {

    @InjectMocks
    private ESCategoryDocumentRepository esCategoryDocumentRepository;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NavigationServiceConfig navigationServiceConfig;
    @Mock
    private RestHighLevelClient restHighLevelClient;
    @Mock
    private GetResponse getResponse;
    @Mock
    private SearchResponse searchResponse;

    @Test
    @SneakyThrows
    public void shouldCallCloseClientMethodAtLeastOnce() {
        esCategoryDocumentRepository.closeClient();
        verify(restHighLevelClient).close();
    }

    @Test(expected = CategoryNotFoundException.class)
    @SneakyThrows
    public void shouldThrowExceptionWhenCategoryDocumentIsNotAvailable() {
        when(getResponse.isExists()).thenReturn(false);
        when(restHighLevelClient.get(any(GetRequest.class), any(RequestOptions.class))).thenReturn(getResponse);
        esCategoryDocumentRepository.getCategoryDocument("catId");
    }

    @Test(expected = CategoryNotFoundException.class)
    @SneakyThrows
    public void shouldThrowExceptionWhenCategoryDocumentsAreNotAvailable() {
        when(restHighLevelClient.search(any(SearchRequest.class), any(RequestOptions.class))).thenReturn(searchResponse);
        when(navigationServiceConfig.getElasticSearchConfig().getIndexName()).thenReturn("index");
        when(navigationServiceConfig.getElasticSearchConfig().getDocumentType6()).thenReturn("_doc");
        when(searchResponse.getHits()).thenReturn(new SearchHits(new SearchHit[0], 0, 1));
        esCategoryDocumentRepository.getCategoryDocuments(Collections.singletonList("catId"));
    }

    @Test
    @SneakyThrows
    public void shouldReturnCategoryDocumentsWhenTheyAreAvailable() {
        CategoryDocument input = CategoryDocument.builder().id("cat1").build();

        BytesReference source = new BytesArray(new ObjectMapper().writeValueAsString(input));
        SearchHit searchHit = new SearchHit(1);
        searchHit.sourceRef(source);
        SearchHits searchHits = new SearchHits(new SearchHit[]{searchHit}, 5, 10);

        when(searchResponse.getHits()).thenReturn(searchHits);
        when(getResponse.isExists()).thenReturn(false);
        when(restHighLevelClient.search(any(SearchRequest.class), any(RequestOptions.class))).thenReturn(searchResponse);
        when(navigationServiceConfig.getElasticSearchConfig().getIndexName()).thenReturn("index");
        when(navigationServiceConfig.getElasticSearchConfig().getDocumentType6()).thenReturn("_doc");
        List<CategoryDocument> categoryDocuments = esCategoryDocumentRepository.getCategoryDocuments(Collections.singletonList("catId"));
        assertNotNull(categoryDocuments);
        assertThat(categoryDocuments.size(), is(1));
    }

    @Test
    @SneakyThrows
    public void shouldReturnCategoryDocumentWhenItIsAvailable() {
        when(getResponse.isExists()).thenReturn(true);
        when(getResponse.getSourceAsString()).thenReturn("{ \"key\":\"true\"}");
        when(restHighLevelClient.get(any(GetRequest.class), any(RequestOptions.class))).thenReturn(getResponse);
        CategoryDocument categoryDocument = esCategoryDocumentRepository.getCategoryDocument("catId");
        assertNotNull(categoryDocument);
    }

    @Test(expected = CategoryNotFoundException.class)
    @SneakyThrows
    public void shouldThrowExceptionWhenInvalidCategoryDocumentFound() {
        when(getResponse.isExists()).thenReturn(true);
        when(getResponse.getSourceAsString()).thenReturn("{ \"true\"}");
        when(restHighLevelClient.get(any(GetRequest.class), any(RequestOptions.class))).thenReturn(getResponse);
        CategoryDocument categoryDocument = esCategoryDocumentRepository.getCategoryDocument("catId");
        assertNotNull(categoryDocument);
    }
}

package com.sixthday.navigation.batch.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.navigation.batch.vo.CategoryDocuments;
import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import com.sixthday.navigation.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.elasticsearch.repository.CategoryRepository;
import com.sixthday.navigation.exceptions.NavigationBatchServiceException;
import lombok.SneakyThrows;
import org.elasticsearch.action.search.*;
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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RestHighLevelClient.class})
@PowerMockIgnore("javax.management.*")
public class CategoryRepositoryTest {

    @InjectMocks
    private CategoryRepository categoryRepository;

    @Mock
    private RestHighLevelClient restHighLevelClient;
    @Mock
    private SearchResponse searchResponse;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NavigationBatchServiceConfig navigationBatchServiceConfig;

    @Test
    @SneakyThrows
    public void shouldCallCloseClient() {
        categoryRepository.closeClient();
        verify(restHighLevelClient).close();
    }

    @Test
    public void shouldReturnNullWhenCategoryDocumentDoesNotAvailable() throws IOException {
        SearchHit[] searchHit = new SearchHit[1];
        searchHit[0] = new SearchHit(10);
        SearchHits searchHits = new SearchHits(searchHit, 10, 10);

        when(searchResponse.getHits()).thenReturn(searchHits);
        when(navigationBatchServiceConfig.getElasticSearchConfig().getCategoryIndex().getName()).thenReturn("category_index");
        when(restHighLevelClient.search(any(SearchRequest.class), any(RequestOptions.class))).thenReturn(searchResponse);

        assertNull(categoryRepository.getCategoryDocument("notExists"));
    }

    @SneakyThrows
    @Test(expected = NavigationBatchServiceException.class)
    public void shouldThrowExceptionWhenGetCategoryDocumentsWithValidScrollIdCalled() {
        when(searchResponse.getHits()).thenThrow(IOException.class);
        when(restHighLevelClient.scroll(any(SearchScrollRequest.class), any(RequestOptions.class))).thenReturn(searchResponse);
        categoryRepository.getCategoryDocuments("-1");
    }

    @Test
    @SneakyThrows
    public void shouldGetCategoryDocumentsWithValidScrollId() {
        SearchHit[] searchHit = new SearchHit[1];
        searchHit[0] = new SearchHit(10);
        SearchHits searchHits = new SearchHits(searchHit, 10, 10);

        when(searchResponse.getHits()).thenReturn(searchHits);
        when(restHighLevelClient.scroll(any(SearchScrollRequest.class), any(RequestOptions.class))).thenReturn(searchResponse);

        assertNotNull(categoryRepository.getCategoryDocuments("10"));
    }

    @Test
    @SneakyThrows
    public void shouldGetCategoryDocumentWithValidCategoryId() {
        CategoryDocument input = CategoryDocument.builder().id("cat1").build();

        BytesReference source = new BytesArray(new ObjectMapper().writeValueAsString(input));
        SearchHit searchHit = new SearchHit(1);
        searchHit.sourceRef(source);
        SearchHits searchHits = new SearchHits(new SearchHit[]{searchHit}, 5, 10);

        when(searchResponse.getHits()).thenReturn(searchHits);
        when(restHighLevelClient.search(any(SearchRequest.class), any(RequestOptions.class))).thenReturn(searchResponse);
        when(navigationBatchServiceConfig.getElasticSearchConfig().getCategoryIndex().getName()).thenReturn("category_index");
        when(navigationBatchServiceConfig.elasticSearchObjectMapper().readValue(anyString(), eq(CategoryDocument.class))).thenReturn(input);

        CategoryDocument output = categoryRepository.getCategoryDocument("cat1");

        assertNotNull(output);
        assertThat(output.getId(), is("cat1"));
    }

    @Test(expected = NullPointerException.class)
    @SneakyThrows
    public void shouldThrowNPEWhenGetCategoryDocumentWithInValidCategoryId() {
        when(restHighLevelClient.search(any(SearchRequest.class), any(RequestOptions.class))).thenThrow(IOException.class);
        when(navigationBatchServiceConfig.getElasticSearchConfig().getCategoryIndex().getName()).thenReturn("category_index");

        categoryRepository.getCategoryDocument("cat1");
    }

    @Test(expected = NullPointerException.class)
    @SneakyThrows
    public void shouldThrowNPEWhenNoOfDocumentsCalled() {
        when(restHighLevelClient.search(any(SearchRequest.class), any(RequestOptions.class))).thenThrow(IOException.class);
        when(navigationBatchServiceConfig.getElasticSearchConfig().getCategoryIndex().getName()).thenReturn("category_index");

        categoryRepository.getCategoryDocuments(2);
    }

    @Test
    @SneakyThrows
    public void shouldGetNoOfDocuments() {
        CategoryDocument input = CategoryDocument.builder().id("cat1").build();

        BytesReference source = new BytesArray(new ObjectMapper().writeValueAsString(input));
        SearchHit searchHit1 = new SearchHit(1);
        searchHit1.sourceRef(source);

        SearchHit searchHit2 = new SearchHit(1);
        searchHit2.sourceRef(source);

        SearchHits searchHits = new SearchHits(new SearchHit[]{searchHit1, searchHit2}, 5, 10);

        when(searchResponse.getHits()).thenReturn(searchHits);
        when(restHighLevelClient.search(any(SearchRequest.class), any(RequestOptions.class))).thenReturn(searchResponse);
        when(navigationBatchServiceConfig.getElasticSearchConfig().getCategoryIndex().getName()).thenReturn("category_index");
        when(navigationBatchServiceConfig.elasticSearchObjectMapper().readValue(anyString(), eq(CategoryDocument.class))).thenReturn(input);

        CategoryDocuments categoryDocuments = categoryRepository.getCategoryDocuments(2);

        assertNotNull(categoryDocuments);
        assertThat(categoryDocuments.getCategoryDocumentList().size(), is(2));
    }
}

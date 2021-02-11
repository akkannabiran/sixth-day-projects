package com.sixthday.category.elasticsearch.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.category.elasticsearch.config.ElasticSearchConfig;
import com.sixthday.category.exceptions.CategoryDocumentNotFoundException;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.models.ContextualProperty;
import lombok.SneakyThrows;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.*;

import static com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument.DOCUMENT_TYPE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({SearchSourceBuilder.class, SearchRequest.class, SearchHit.class, RestHighLevelClient.class})
public class ESCategoryDocumentRepositoryTest {
    private static String CATEGORY_INDEX = "category_index";
    private static String INVALID_CATEGORY_DOCUMENT_AS_STRING = "\"id\":\"cat1\",\"displayName\":\"Ralph Lauren\",\"url\":\"/Ralph-Lauren/Designers/cat1/c.cat\"";
    private final ObjectMapper objectMapper = new ObjectMapper();
    Set<String> CATEGORY_ID_LIST = new HashSet<>();
    private String CATEGORY_ID = "catId1";

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ElasticSearchConfig elasticSearchConfig;

    @Mock
    private RestHighLevelClient restHighLevelClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SearchSourceBuilder categoriesSearchSourceBuilder;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SearchRequest categoriesSearchRequest;

    @Mock
    private SearchResponse mockSearchResponse;

    @Mock
    private GetResponse categoryResponse;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private GetRequest categoryRequest;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ElasticSearchConfig.CategoryConfig categoryConfig;

    @InjectMocks
    private ESCategoryDocumentRepository esCategoryDocumentRepository;

    @Mock
    private SearchHit searchHit;
    private CategoryDocument categoryDocument;

    @Before
    public void setUp() throws Exception {
        String driveToSubcategoryId = "";
        CATEGORY_ID_LIST.add("cat1");
        CATEGORY_ID_LIST.add("cat2");

        categoryDocument = CategoryDocument.builder()
                .id("myCat")
                .name("Category Name")
                .templateType("some template type")
                .longDescription("http://some-redirect-url")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .alternateSeoName("some alternate seo name")
                .seoTitleOverride("some seo title override")
                .canonicalUrl("/some_canonical_url/")
                .children(Arrays.asList("child1", "child2"))
                .contextualProperties(Arrays.asList(new ContextualProperty("parentCategoryId", "DesktopAlternateName",
                        "MobileAlternateName", Arrays.asList("childCategory1", "childCategory2"),
                        driveToSubcategoryId, false)))
                .build();

        String code = "for ( int i = 0; i < params.ids.size(); i++) {if (params['_source']['id'] == params.ids[i]) { return (params.ids.size() + i);} }";

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("ids", new ArrayList<>(CATEGORY_ID_LIST));
        }};
        when(elasticSearchConfig.getCategoryConfig()).thenReturn(categoryConfig);
        when(categoryConfig.getIndexName()).thenReturn(CATEGORY_INDEX);
        when(categoryConfig.getDocumentType()).thenReturn(DOCUMENT_TYPE);
        when(restHighLevelClient.get(any(GetRequest.class), any(RequestOptions.class))).thenReturn(categoryResponse);
        when(categoryResponse.isExists()).thenReturn(true);
        when(categoriesSearchRequest.source(eq(categoriesSearchSourceBuilder))).thenReturn(categoriesSearchRequest);
        when(restHighLevelClient.search(any(SearchRequest.class), any(RequestOptions.class))).thenReturn(mockSearchResponse);
        searchHit = mock(SearchHit.class);
        SearchHit[] hits = new SearchHit[]{searchHit, searchHit};
        when(mockSearchResponse.getHits()).thenReturn(new SearchHits(hits, 2, 2));

    }

    @Test
    @SneakyThrows
    public void shouldGetCategoryDocumentsWhenCategoryIdsAvailable() {
        when(searchHit.getSourceAsString()).thenReturn(objectMapper.writeValueAsString(categoryDocument));
        List<CategoryDocument> categoryDocuments = esCategoryDocumentRepository.getCategoryDocuments(CATEGORY_ID_LIST);
        assertEquals(categoryDocuments.size(), 2);
    }

    @Test
    @SneakyThrows
    public void shouldGetCategoryDocumentWhenCategoryIdAvailable() {
        when(categoryResponse.getSourceAsString()).thenReturn(objectMapper.writeValueAsString(categoryDocument));
        CategoryDocument categoryDocument = esCategoryDocumentRepository.getCategoryDocument(CATEGORY_ID);
        assertNotNull(categoryDocument);
    }

    @Test
    public void shouldAssertCategoryDocumentstoZeroWhenSearchHitsAreEmpty() {
        when(mockSearchResponse.getHits()).thenReturn(SearchHits.empty());
        List<CategoryDocument> categoryDocuments = esCategoryDocumentRepository.getCategoryDocuments(CATEGORY_ID_LIST);
        assertEquals(categoryDocuments.size(), 0);
    }

    @Test
    public void shouldCallCloseClientMethodAtLeastOnce() throws IOException {
        esCategoryDocumentRepository.closeClient();
        verify(restHighLevelClient, atLeast(1)).close();
    }

    @Test
    public void shouldReturnEmptyListAndNotThrowExceptionWhenCategoryIdsAvailableWithInvalidDocumentFormat() {
        when(searchHit.getSourceAsString()).thenReturn(INVALID_CATEGORY_DOCUMENT_AS_STRING);
        assertThat(esCategoryDocumentRepository.getCategoryDocuments(CATEGORY_ID_LIST), equalTo(Collections.emptyList()));
    }

    @Test
    public void shouldReturnNullWhenTheCategoryDocumentIsInInvalidDocumentFormat() {
        when(categoryResponse.isExists()).thenReturn(false);
        Assert.assertNull(esCategoryDocumentRepository.getCategoryDocument(CATEGORY_ID));
    }

    @Test
    public void shouldReturnNullAndLogMessageWhenErrorOccurredWhileParsingDocument() {
        when(categoryResponse.getSourceAsString()).thenReturn(INVALID_CATEGORY_DOCUMENT_AS_STRING);
        Assert.assertNull(esCategoryDocumentRepository.getCategoryDocument(CATEGORY_ID));
    }

    @SneakyThrows
    @Test(expected = CategoryDocumentNotFoundException.class)
    public void testShouldThrowExceptionWhenRestClientFailsToPullDocument() {
        when(restHighLevelClient.get(any(GetRequest.class), any(RequestOptions.class))).thenThrow(IOException.class);
        esCategoryDocumentRepository.getCategoryDocument("some category");
    }

    @SneakyThrows
    @Test(expected = CategoryDocumentNotFoundException.class)
    public void testShouldThrowExceptionWhenRestClientFailsToPullDocuments() {
        when(restHighLevelClient.search(any(SearchRequest.class), any(RequestOptions.class))).thenThrow(IOException.class);
        esCategoryDocumentRepository.getCategoryDocuments(new HashSet<>());
    }

    @Test
    @SneakyThrows
    public void shouldReturnNullWhenIsExistsIsTrue() {
        when(restHighLevelClient.get(any(GetRequest.class), any(RequestOptions.class))).thenReturn(categoryResponse);
        when(categoryResponse.isExists()).thenReturn(true);

        CategoryDocument categoryDocument = esCategoryDocumentRepository.getCategoryDocument("some category id");
        assertNull(categoryDocument);
    }

    @Test
    @SneakyThrows
    public void shouldReturnNullWhenIsExistsIsFalse() {
        when(restHighLevelClient.get(any(GetRequest.class), any(RequestOptions.class))).thenReturn(categoryResponse);
        when(categoryResponse.isExists()).thenReturn(true);

        CategoryDocument categoryDocument = esCategoryDocumentRepository.getCategoryDocument("some category id");
        assertNull(categoryDocument);
    }

    @Test
    @SneakyThrows
    public void shouldReturnNullWhenGetResponseIsNull() {
        when(restHighLevelClient.get(any(GetRequest.class), any(RequestOptions.class))).thenReturn(null);

        CategoryDocument categoryDocument = esCategoryDocumentRepository.getCategoryDocument("some category id");
        assertNull(categoryDocument);
    }
}
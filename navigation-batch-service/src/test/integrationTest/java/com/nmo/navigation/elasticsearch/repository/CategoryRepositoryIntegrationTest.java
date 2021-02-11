package com.sixthday.navigation.elasticsearch.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.navigation.NavigationBatchApplication;
import com.sixthday.navigation.batch.vo.CategoryDocuments;
import com.sixthday.navigation.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.integration.receiver.config.SubscriberConfiguration;
import lombok.SneakyThrows;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {NavigationBatchApplication.class})
@DirtiesContext
public class CategoryRepositoryIntegrationTest {

    private static final String CATEGORY_THAT_WILL_BE_RETRIEVED = "categoryThatWillBeRetrieved";
    private static final String CATEGORY_THAT_WILL_BE_RETRIEVED_WITH_NO_DELETED_FLAG = "categoryThatWillBeRetrievedWithNoDeletedFlag";
    private static final String DELETED_CATEGORY = "deletedCategory";
    private static final String HIDDEN_CATEGORY = "hiddenCategory";
    private static final String NO_RESULTS_CATEGORY = "noResultsCategory";
    private static final String CATEGORY_WITH_CHILDREN = "categoryWithChildren";

    @Autowired
    private SubscriberConfiguration subscriberConfiguration;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    public void shouldReturnNonHiddenNoResultsAndNoChildrenCategoryThatAreNotDeleted() throws Exception {
        HashMap<String, String> categoryDocuments = insertCategoriesToElasticSearch();

        insertCategoriesToES(categoryDocuments);

        CategoryDocuments categories = categoryRepository.getCategoryDocuments(1000);

        assertThat(categories.getCategoryDocumentList(), hasItem(hasProperty("id", is(CATEGORY_THAT_WILL_BE_RETRIEVED))));
        assertThat(categories.getCategoryDocumentList(), hasItem(hasProperty("id", is(CATEGORY_THAT_WILL_BE_RETRIEVED_WITH_NO_DELETED_FLAG))));
        assertThat(categories.getCategoryDocumentList(), hasItem(hasProperty("id", is(CATEGORY_WITH_CHILDREN))));

        assertThat(categories.getCategoryDocumentList(), not(hasItem(hasProperty("id", is(DELETED_CATEGORY)))));
        assertThat(categories.getCategoryDocumentList(), not(hasItem(hasProperty("id", is(HIDDEN_CATEGORY)))));
        assertThat(categories.getCategoryDocumentList(), not(hasItem(hasProperty("id", is(NO_RESULTS_CATEGORY)))));
    }

    @Test
    public void shouldNotRetrieveCategoriesByIdThatAreDeleted() throws Exception {
        HashMap<String, String> categoryDocuments = new HashMap<String, String>() {{
            put("deletedCat01", new ObjectMapper().writeValueAsString(CategoryDocument.builder().id("deletedCat01")
                    .isDeleted(true)
                    .build()));
        }};

        insertCategoriesToES(categoryDocuments);

        CategoryDocument category = categoryRepository.getCategoryDocument("deletedCat01");

        assertNull(category);
    }

    @Test
    public void shouldRetrieveCategoriesByIdThatDoNotHaveDeletedFlag() throws Exception {
        HashMap<String, String> categoryDocuments = new HashMap<String, String>() {{
            put(CATEGORY_THAT_WILL_BE_RETRIEVED, new ObjectMapper().writeValueAsString(CategoryDocument.builder().id(CATEGORY_THAT_WILL_BE_RETRIEVED)
                    .isDeleted(false)
                    .build()).replace("\"isDeleted\":false,", ""));
        }};

        insertCategoriesToES(categoryDocuments);

        CategoryDocument category = categoryRepository.getCategoryDocument(CATEGORY_THAT_WILL_BE_RETRIEVED);

        assertThat(category.getId(), is(CATEGORY_THAT_WILL_BE_RETRIEVED));
    }

    @Test
    public void shouldRetrieveCategoriesByIdThatAreNotDeleted() throws Exception {
        HashMap<String, String> categoryDocuments = new HashMap<String, String>() {{
            put(CATEGORY_THAT_WILL_BE_RETRIEVED, new ObjectMapper().writeValueAsString(CategoryDocument.builder().id(CATEGORY_THAT_WILL_BE_RETRIEVED)
                    .isDeleted(false)
                    .build()));
        }};

        insertCategoriesToES(categoryDocuments);

        CategoryDocument category = categoryRepository.getCategoryDocument(CATEGORY_THAT_WILL_BE_RETRIEVED);

        assertThat(category.getId(), is(CATEGORY_THAT_WILL_BE_RETRIEVED));
    }

    @NotNull
    private HashMap<String, String> insertCategoriesToElasticSearch() throws JsonProcessingException {
        CategoryDocument categoryThatWillBeRetrieved = CategoryDocument.builder().id(CATEGORY_THAT_WILL_BE_RETRIEVED)
                .isDeleted(false)
                .hidden(false)
                .noResults(false)
                .build();
        CategoryDocument deletedCategory = CategoryDocument.builder().id(DELETED_CATEGORY)
                .isDeleted(true)
                .hidden(false)
                .noResults(false)
                .build();
        CategoryDocument hiddenCategory = CategoryDocument.builder().id(HIDDEN_CATEGORY)
                .isDeleted(false)
                .hidden(true)
                .noResults(false)
                .build();
        CategoryDocument noResultsCategory = CategoryDocument.builder().id(NO_RESULTS_CATEGORY)
                .isDeleted(false)
                .hidden(false)
                .noResults(true)
                .build();
        CategoryDocument categoryWithChildren = CategoryDocument.builder().id(CATEGORY_WITH_CHILDREN)
                .isDeleted(false)
                .hidden(false)
                .noResults(false)
                .children((Collections.singletonList("cat00001")))
                .build();
        CategoryDocument categoryThatWillBeRetrievedWithNoDeletedFlag = CategoryDocument.builder().id(CATEGORY_THAT_WILL_BE_RETRIEVED_WITH_NO_DELETED_FLAG)
                .isDeleted(false)
                .hidden(false)
                .noResults(false)
                .build();
        return new HashMap<String, String>() {{
            put(CATEGORY_THAT_WILL_BE_RETRIEVED, new ObjectMapper().writeValueAsString(categoryThatWillBeRetrieved).replace(",\"children\":null", ""));
            put(CATEGORY_THAT_WILL_BE_RETRIEVED_WITH_NO_DELETED_FLAG, new ObjectMapper().writeValueAsString(categoryThatWillBeRetrievedWithNoDeletedFlag).replace(",\"children\":null", "").replace("\"isDeleted\":false,", ""));
            put(DELETED_CATEGORY, new ObjectMapper().writeValueAsString(deletedCategory).replace(",\"children\":null", ""));
            put(HIDDEN_CATEGORY, new ObjectMapper().writeValueAsString(hiddenCategory).replace(",\"children\":null", ""));
            put(NO_RESULTS_CATEGORY, new ObjectMapper().writeValueAsString(noResultsCategory).replace(",\"children\":null", ""));
            put(CATEGORY_WITH_CHILDREN, new ObjectMapper().writeValueAsString(categoryWithChildren));
        }};
    }

    @After
    @SneakyThrows
    public void tearDown() {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.add(new DeleteRequest(
                subscriberConfiguration.getNavigationBatchServiceConfig().getElasticSearchConfig().getCategoryIndex().getName(), "_doc", CATEGORY_THAT_WILL_BE_RETRIEVED));
        bulkRequest.add(new DeleteRequest(
                subscriberConfiguration.getNavigationBatchServiceConfig().getElasticSearchConfig().getCategoryIndex().getName(), "_doc", CATEGORY_THAT_WILL_BE_RETRIEVED_WITH_NO_DELETED_FLAG));
        bulkRequest.add(new DeleteRequest(
                subscriberConfiguration.getNavigationBatchServiceConfig().getElasticSearchConfig().getCategoryIndex().getName(), "_doc", DELETED_CATEGORY));
        bulkRequest.add(new DeleteRequest(
                subscriberConfiguration.getNavigationBatchServiceConfig().getElasticSearchConfig().getCategoryIndex().getName(), "_doc", HIDDEN_CATEGORY));
        bulkRequest.add(new DeleteRequest(
                subscriberConfiguration.getNavigationBatchServiceConfig().getElasticSearchConfig().getCategoryIndex().getName(), "_doc", NO_RESULTS_CATEGORY));
        bulkRequest.add(new DeleteRequest(
                subscriberConfiguration.getNavigationBatchServiceConfig().getElasticSearchConfig().getCategoryIndex().getName(), "_doc", "deletedCat01"));
        bulkRequest.add(new DeleteRequest(
                subscriberConfiguration.getNavigationBatchServiceConfig().getElasticSearchConfig().getCategoryIndex().getName(), "_doc", CATEGORY_WITH_CHILDREN));

        restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    @SneakyThrows
    private void insertCategoriesToES(Map<String, String> categories) {
        BulkRequest bulkRequest = new BulkRequest();
        for (Map.Entry<String, String> categoryDocument : categories.entrySet()) {
            bulkRequest.add(new IndexRequest(subscriberConfiguration.getNavigationBatchServiceConfig().getElasticSearchConfig().getCategoryIndex().getName(), subscriberConfiguration.getNavigationBatchServiceConfig().getElasticSearchConfig().getCategoryIndex().getDocumentType6(), categoryDocument.getKey())
                    .source(categoryDocument.getValue(), XContentType.JSON));
        }

        restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);

        restHighLevelClient.indices().refresh(new RefreshRequest("category_index"), RequestOptions.DEFAULT);
    }
}

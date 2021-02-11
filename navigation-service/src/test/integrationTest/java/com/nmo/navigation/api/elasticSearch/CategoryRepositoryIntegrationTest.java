package com.sixthday.navigation.api.elasticSearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.navigation.Application;
import com.sixthday.navigation.api.controllers.elasticsearch.documents.CategoryDocumentBuilder;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.repository.CategoryRepository;
import com.sixthday.navigation.integration.config.SubscriberConfiguration;
import lombok.SneakyThrows;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class})
@DirtiesContext
public class CategoryRepositoryIntegrationTest {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubscriberConfiguration subscriberConfiguration;

    @Test
    public void shouldReturnOnlyNonDeletedCategoriesOrCategoriesThatDontHaveDeletedFlagInPassedInOrder() throws Exception {
        addCategoriesToElasticSearch();

        List<CategoryDocument> categories = categoryRepository.getCategoryDocuments(new ArrayList<String>() {{
            add("deletedCat1");
            add("catWithNoDeletedFlag");
            add("cat2");
        }});

        assertThat(categories.size(), is(2));
        assertThat(categories.get(0).getId(), is("catWithNoDeletedFlag"));
        assertThat(categories.get(1).getId(), is("cat2"));
    }
    
    @Test
    public void shouldReturnCategoriesWithApplicableFilters() throws Exception {
        addCategoriesToElasticSearch();

        List<CategoryDocument> categories = categoryRepository.getCategoryDocuments(new ArrayList<String>() {{
            add("deletedCat1");
            add("catWithNoDeletedFlag");
            add("cat2");
        }});

        assertThat(categories.size(), is(2));
        CategoryDocument category1 = categories.get(0);
        assertThat(category1.getId(), is("catWithNoDeletedFlag"));
        assertThat(category1.getApplicableFilters().size(), is(1));
        assertThat(category1.getApplicableFilters().get(0).getDefaultName(), is("MyFilter2"));
        assertThat(category1.getApplicableFilters().get(0).getAlternateName(), is("MyAltName"));
        assertThat(category1.getApplicableFilters().get(0).getValues(), Matchers.nullValue());
        
        CategoryDocument category2 = categories.get(1);
        assertThat(category2.getId(), is("cat2"));
        assertThat(category2.getApplicableFilters().size(), is(1));
        assertThat(category2.getApplicableFilters().get(0).getDefaultName(), is("MyFilter1"));
        assertThat(category2.getApplicableFilters().get(0).getAlternateName(), Matchers.nullValue());
        assertThat(category2.getApplicableFilters().get(0).getValues(), is(Arrays.asList("Value1", "Value2")));        
        
        updateCategory(new CategoryDocumentBuilder().withId("cat2").withFilter("MyFilter1", null, null, Arrays.asList("Value2", "Value1", "Value3")).build());
        
        List<CategoryDocument> updatedCats = categoryRepository.getCategoryDocuments(Arrays.asList("cat2"));
        CategoryDocument categoryWithReOrderedFilterValues = updatedCats.get(0);
        assertThat(categoryWithReOrderedFilterValues.getId(), is("cat2"));
        assertThat("Reordered filter values are saved and retrieved from ES",
                categoryWithReOrderedFilterValues.getApplicableFilters().get(0).getValues(), is(Arrays.asList("Value2", "Value1", "Value3")));
        
        updateCategory(new CategoryDocumentBuilder().withId("cat2").withFilter("MyFilter1", null, null, Collections.emptyList()).build());
        updatedCats = categoryRepository.getCategoryDocuments(Arrays.asList("cat2"));
        CategoryDocument categoriesWithEmptyFilterValueList = updatedCats.get(0);
        assertThat(categoriesWithEmptyFilterValueList.getId(), is("cat2"));
        assertThat("Filter values are removed and emptly list is fetched from ES",
                categoriesWithEmptyFilterValueList.getApplicableFilters().get(0).getValues(), is(Collections.emptyList()));
        
    }
    
    @After
    @SneakyThrows
    public void tearDown() {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.add(new DeleteRequest(
                subscriberConfiguration.getElasticSearchConfig().getIndexName(),
                CategoryDocument.DOCUMENT_TYPE,
                "deletedCat1"));
        bulkRequest.add(new DeleteRequest(
                subscriberConfiguration.getElasticSearchConfig().getIndexName(),
                CategoryDocument.DOCUMENT_TYPE,
                "cat2"));
        bulkRequest.add(new DeleteRequest(
                subscriberConfiguration.getElasticSearchConfig().getIndexName(),
                CategoryDocument.DOCUMENT_TYPE,
                "catWithNoDeletedFlag"));
        client.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    private void addCategoriesToElasticSearch() throws IOException {
        BulkRequest bulkRequestForIndex = new BulkRequest();
        bulkRequestForIndex.add(new IndexRequest(subscriberConfiguration.getElasticSearchConfig().getIndexName(), CategoryDocument.DOCUMENT_TYPE, "deletedCat1")
                .source(new ObjectMapper().writeValueAsString(new CategoryDocumentBuilder().withId("deletedCat1").withDeletedFlag(true).build()), XContentType.JSON));

        bulkRequestForIndex.add(new IndexRequest(subscriberConfiguration.getElasticSearchConfig().getIndexName(), CategoryDocument.DOCUMENT_TYPE, "cat2")
                .source(new ObjectMapper().writeValueAsString(new CategoryDocumentBuilder().withId("cat2").withFilter("MyFilter1", null, null, Arrays.asList("Value1", "Value2")).build()), XContentType.JSON));

        bulkRequestForIndex.add(new IndexRequest(subscriberConfiguration.getElasticSearchConfig().getIndexName(), CategoryDocument.DOCUMENT_TYPE, "catWithNoDeletedFlag")
                .source(new ObjectMapper().writeValueAsString(new CategoryDocumentBuilder().withId("catWithNoDeletedFlag").withFilter("MyFilter2", "MyAltName", null, null).build()).replace(",\"isDeleted\":false", ""), XContentType.JSON));

        client.bulk(bulkRequestForIndex, RequestOptions.DEFAULT);

        client.indices().refresh(new RefreshRequest("category_index"), RequestOptions.DEFAULT);
    }
    
    private void updateCategory(CategoryDocument category)  throws IOException {
        BulkRequest bulkRequestForIndex = new BulkRequest();
        bulkRequestForIndex.add(new IndexRequest(subscriberConfiguration.getElasticSearchConfig().getIndexName(), CategoryDocument.DOCUMENT_TYPE, category.getId())
                .source(new ObjectMapper().writeValueAsString(category), XContentType.JSON));

        client.bulk(bulkRequestForIndex, RequestOptions.DEFAULT);
        client.indices().refresh(new RefreshRequest("category_index"), RequestOptions.DEFAULT);
    }
    
}

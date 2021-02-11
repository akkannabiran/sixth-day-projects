package com.sixthday.store.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.store.config.StoreDetailsConfig;
import com.sixthday.store.data.StoreDocumentBuilder;
import com.sixthday.store.data.StoreSkuInventoryDocumentBuilder;
import com.sixthday.store.models.storeindex.StoreDocument;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryDocument;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({ElasticsearchRepository.class,RestHighLevelClient.class, SearchSourceBuilder.class,Logger.class,SearchResponse.class,SearchHit.class,SearchHits.class,StoreDetailsConfig.class})
public class ElasticsearchRepositoryTest {

    private StoreDetailsConfig config = PowerMockito.mock(StoreDetailsConfig.class);

    private RestHighLevelClient client = PowerMockito.mock(RestHighLevelClient.class);

    SearchSourceBuilder searchSourceBuilder = PowerMockito.mock(SearchSourceBuilder.class);

    SearchHits searchHits = PowerMockito.mock(SearchHits.class);

    SearchResponse searchResponse = PowerMockito.mock(SearchResponse.class);

    Logger logger = PowerMockito.mock(Logger.class);

    SearchHit storeHit = PowerMockito.mock(SearchHit.class);


    private ElasticsearchRepository elasticsearchRepository = new ElasticsearchRepository(config,client);

    @Before
    public void setUp() {
        StoreDetailsConfig.ElasticSearchConfig elasticSearchConfig = new StoreDetailsConfig.ElasticSearchConfig();
        elasticSearchConfig.setStoreIndexName("storeIndex");
        elasticSearchConfig.setStoreSkuInventoryIndexName("storeInventoryIndex");
        when(config.getElasticSearchConfig()).thenReturn(elasticSearchConfig);
    }

    @Test
    public void shouldReturnStoreDocumentsWhenElasticSearchHasStoreData () throws Exception {
        List<String > storeNumber = new ArrayList<>();
        storeNumber.add("1");
        storeNumber.add("2");

        String sourceString = new StoreDocumentBuilder()
                .withId("07/SL")
                .withDisplayable(false)
                .withEligibleForBOPS(false)
                .withStoreEventDocument()
                .withEventDescription("someDescription")
                .withEventName("someName")
                .done()
                .buildAsJsonString();

        when(searchSourceBuilder.query(anyObject())).thenReturn(searchSourceBuilder);
        when(client.search(anyObject(), (RequestOptions) anyObject())).thenReturn(searchResponse);
        when(storeHit.getSourceAsString()).thenReturn(sourceString);
        when(searchResponse.getHits()).thenReturn(searchHits);
        when(searchResponse.getHits().getHits()).thenReturn(new SearchHit[]{storeHit});
        List<StoreDocument> storeDocuments = elasticsearchRepository.filterStoresForStoreNumbers(storeNumber,new String("serchHits"));

        assertNotNull(storeDocuments);
        assertThat(storeDocuments, IsCollectionWithSize.hasSize(1));
    }

    @Test
    public void shouldReturnStoreGetStoreById() throws IOException {
       String storeNumber = "1";

        String sourceString = new StoreDocumentBuilder()
                .withId("07/SL")
                .withDisplayable(false)
                .withEligibleForBOPS(false)
                .withStoreEventDocument()
                .withEventDescription("someDescription")
                .withEventName("someName")
                .done()
                .buildAsJsonString();

        when(searchSourceBuilder.query(anyObject())).thenReturn(searchSourceBuilder);
        when(client.search(anyObject(), (RequestOptions) anyObject())).thenReturn(searchResponse);
        when(storeHit.getSourceAsString()).thenReturn(sourceString);
        when(searchResponse.getHits()).thenReturn(searchHits);
        when(searchResponse.getHits().getHits()).thenReturn(new SearchHit[]{storeHit});
        List<StoreDocument> storeDocuments = elasticsearchRepository.getStoresById(storeNumber);
        assertNotNull(storeDocuments);
        assertThat(storeDocuments, IsCollectionWithSize.hasSize(1));
    }

    @Test
    public void shouldReturnStoreDocumentsGetAllStoresSkus() throws IOException {
        String storeNumber = "1";

        String sourceString =new ObjectMapper().writeValueAsString(new StoreSkuInventoryDocumentBuilder()
                .build());

        when(searchSourceBuilder.query(anyObject())).thenReturn(searchSourceBuilder);
        when(client.search(anyObject(), (RequestOptions) anyObject())).thenReturn(searchResponse);
        when(storeHit.getSourceAsString()).thenReturn(sourceString);
        when(searchResponse.getHits()).thenReturn(searchHits);
        when(searchResponse.getHits().getHits()).thenReturn(new SearchHit[]{storeHit});
        List<StoreSkuInventoryDocument> storeDocuments = elasticsearchRepository.getAllStoresSkus(Arrays.asList(storeNumber), storeNumber);
        assertNotNull(storeDocuments);
        assertThat(storeDocuments, IsCollectionWithSize.hasSize(1));
    }
}
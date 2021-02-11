package com.sixthday.store.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.store.config.SubscriberConfiguration;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryDocument;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;


@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({StoreSkuInventorySyncRepository.class,RestHighLevelClient.class})
public class StoreSkuInventorySyncRepositoryTest {

    private static final String INDEX_NAME = "index-name";
    private StoreSkuInventoryDocument validStoreSkuInventoryDocument;
    private StoreSkuInventorySyncRepository storeSkuInventorySyncRepository;
    private ObjectMapper genericMapper = new ObjectMapper();

    @Mock
    private SearchRequestBuilder deleteQuerySource;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private SubscriberConfiguration subscriberConfiguration;

    private RestHighLevelClient client = PowerMockito.mock(RestHighLevelClient.class);

    @Mock(answer = RETURNS_DEEP_STUBS)
    private IndexResponse indexResponse;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private IndexRequest indexRequest;

    @Captor
    private ArgumentCaptor<String> setDocArgumentCaptor;

    @Captor
    private ArgumentCaptor<IndexRequest> setDocArgumentCaptorForIndexRequest;

    @Before
    public void setUp() throws Exception {
        validStoreSkuInventoryDocument = new StoreSkuInventoryDocument();
        validStoreSkuInventoryDocument.setId("sku92740103:01");
        validStoreSkuInventoryDocument.setStoreNumber("01");
        validStoreSkuInventoryDocument.setStoreId("01/DT");
        validStoreSkuInventoryDocument.setLocationNumber("1000");
        validStoreSkuInventoryDocument.setQuantity(1);
        validStoreSkuInventoryDocument.setBopsQuantity(8);
        validStoreSkuInventoryDocument.setInventoryLevelCode("2");
        validStoreSkuInventoryDocument.setSkuId("sku92740103");

        when(subscriberConfiguration.getElasticSearchConfig().getStoreSkuInventoryIndexName()).thenReturn(INDEX_NAME);


        when(deleteQuerySource.setIndices(any())).thenReturn(deleteQuerySource);
        when(deleteQuerySource.setTypes(any())).thenReturn(deleteQuerySource);
        when(deleteQuerySource.setQuery(any())).thenReturn(deleteQuerySource);

        storeSkuInventorySyncRepository = new StoreSkuInventorySyncRepository(subscriberConfiguration,client);
    }

    @Test
    public void shouldUpdateStoreSkuInventoryGivenStoreSkuInventoryUpdatedEventType() throws Exception {
        String storeSkuInventoryDocument = genericMapper.writeValueAsString(validStoreSkuInventoryDocument);
        PowerMockito.doNothing().when(client).updateAsync(anyObject(),any(RequestOptions.class),any());
        storeSkuInventorySyncRepository.createOrUpdateStoreSkuInventory(validStoreSkuInventoryDocument);
        IndexRequest actualIndexRequest = storeSkuInventorySyncRepository.createIndexRequest(validStoreSkuInventoryDocument, storeSkuInventoryDocument);
        assertNotNull(actualIndexRequest);
    }

    @Test
    public void shouldDeleteStoreSkuInventoryGivenStoreSkuInventory() throws Exception {
        String storeSkuInventoryDocument = genericMapper.writeValueAsString(validStoreSkuInventoryDocument);
        PowerMockito.doNothing().when(client).deleteAsync(anyObject(),any(RequestOptions.class),any());
        storeSkuInventorySyncRepository.deleteStoreSkuInventory(validStoreSkuInventoryDocument);
        IndexRequest actualIndexRequest = storeSkuInventorySyncRepository.createIndexRequest(validStoreSkuInventoryDocument, storeSkuInventoryDocument);
        assertNotNull(actualIndexRequest);
    }
}
package com.sixthday.store.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.store.config.SubscriberConfiguration;
import com.sixthday.store.data.StoreDocumentBuilder;
import com.sixthday.store.exceptions.StoreUpsertFailedException;
import com.sixthday.store.models.storeindex.StoreDocument;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryDocument;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static com.sixthday.store.models.storeindex.StoreMessage.EventType.STORE_UPSERT;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({StoreSyncRepository.class, RestHighLevelClient.class})
public class StoreSyncRepositoryTest {

    private StoreSyncRepository storeSyncRepository;

    private StoreDocument validStoreDocument;

    private static final String INDEX_NAME = "index-name";
    private StoreSkuInventoryDocument validStoreSkuInventoryDocument;
    private StoreSkuInventorySyncRepository storeSkuInventorySyncRepository;
    private ObjectMapper genericMapper = new ObjectMapper();

    @Mock
    private SearchRequestBuilder deleteQuerySource;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private SubscriberConfiguration subscriberConfiguration;

    private RestHighLevelClient client = PowerMockito.mock(RestHighLevelClient.class);


    @Before
    public void setUp() throws Exception {
        validStoreDocument = prepareStoreDocument();

        when(subscriberConfiguration.getElasticSearchConfig().getStoreIndexName()).thenReturn(INDEX_NAME);

        storeSyncRepository = new StoreSyncRepository(subscriberConfiguration,client);
    }

    private StoreDocument prepareStoreDocument() {
        validStoreDocument = new StoreDocument();
        validStoreDocument.setId("someId");
        validStoreDocument.setStoreNumber("106");
        validStoreDocument.setStoreName("Bellevue");
        validStoreDocument.setAddressLine1("11111 NE 8th Street");
        validStoreDocument.setCity("Bellevue");
        validStoreDocument.setState("WA");
        validStoreDocument.setZipCode("98004");
        validStoreDocument.setPhoneNumber("425-452-3300");
        validStoreDocument.setStoreHours("Mon. 10:00AM - 9:00PM,Tue. 10:00AM - 9:00PM," +
                "Wed. 10:00AM - 9:00PM,Thu. 10:00AM - 9:00PM," +
                "Fri. 10:00AM - 9:00PM,Sat. 10:00AM - 9:00PM,Sun. 12:00PM - 6:00PM"	);
        validStoreDocument.setStoreDescription("<br>Sixthday is a renowned specialty store dedicated" +
                "to merchandise leadership and superior customer service." +
                "We will offer the finest fashion and quality products in a welcoming environment. ");
        return validStoreDocument;
    }


    @Test
    public void shouldCreateOrUpdateStoreSyncRepository() throws JsonProcessingException, StoreUpsertFailedException {
        String sourceString = new StoreDocumentBuilder()
                .withId("07/SL")
                .withDisplayable(false)
                .withEligibleForBOPS(false)
                .withStoreEventDocument()
                .withEventDescription("someDescription")
                .withEventName("someName")
                .done()
                .buildAsJsonString();

        PowerMockito.doNothing().when(client).updateAsync(anyObject(),any(RequestOptions.class),any());
        storeSyncRepository.createOrUpdateStore(validStoreDocument, STORE_UPSERT);
    }
}

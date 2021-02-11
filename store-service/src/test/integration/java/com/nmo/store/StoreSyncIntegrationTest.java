package com.sixthday.store;


import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Map;

import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sixthday.store.data.StoreDocumentBuilder;
import com.sixthday.store.models.storeindex.StoreDocument;
import com.sixthday.store.models.storeindex.StoreEventDocument;
import com.sixthday.store.models.storeindex.StoreMessage.EventType;
import com.sixthday.store.repository.StoreSyncRepository;
import com.sixthday.store.setup.StoreDocumentLoader;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {StoreLocatorServiceApplication.class}, 
  properties = {"spring.cache.type=none" , "spring.cloud.stream.bindings.output.binder=test","sixthday-store-sub.products-foundation-mode=false","spring.cloud.stream.bindings.storeInventoryBySKU.binder=test2"})
@SuppressWarnings("all")
public class StoreSyncIntegrationTest {
    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private StoreSyncRepository skuSyncRepository;

    private StoreDocumentLoader storeDocumentLoader;

    @Before
    public void setUp() {
    	storeDocumentLoader = new StoreDocumentLoader(client);
    }

    @Test
    public void shouldSaveStoreEventsInElasticSearch() throws Exception {
    	StoreDocument storeDocumentWithoutStoreEvents = new StoreDocumentBuilder().build();
    	
    	skuSyncRepository.createOrUpdateStore(storeDocumentWithoutStoreEvents, EventType.STORE_UPSERT);
    	
    	Map<String, Object> storeDocumentFromES = storeDocumentLoader.findById(storeDocumentWithoutStoreEvents.getId());
        ArrayList<StoreEventDocument> storeEvents = (ArrayList<StoreEventDocument>) storeDocumentFromES.get("events");
        assertThat(storeEvents, is(notNullValue()));
        assertThat(storeEvents.size(), is(0));
    }
    
    @Test
	public void shouldSaveStoreEventsInES() throws Exception {
		StoreDocument storeDocumentWithStoreEvents = new StoreDocumentBuilder()
        		.withStoreEventDocument()
                	.withEventDescription("someDescription")
                	.withEventName("someName")
                	.done()
                .build();
        
		skuSyncRepository.createOrUpdateStore(storeDocumentWithStoreEvents, EventType.STORE_UPSERT);
        
		Map<String, Object> storeDocumentFromES = storeDocumentLoader.findById(storeDocumentWithStoreEvents.getId());
        ArrayList<StoreEventDocument> storeEvents = (ArrayList<StoreEventDocument>) storeDocumentFromES.get("events");
        assertThat(storeEvents, is(notNullValue()));
        assertThat(storeEvents.size(), is(1));
	}
}

package com.sixthday.store;

import com.sixthday.store.data.StoreSkuInventoryDocumentBuilder;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryDocument;
import com.sixthday.store.repository.StoreSkuInventorySyncRepository;
import com.sixthday.store.setup.StoreSkuInventoryDocumentLoader;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {StoreLocatorServiceApplication.class},
  properties = {"spring.cache.type=none", "spring.cloud.stream.bindings.output.binder=test","sixthday-store-sub.products-foundation-mode=false","spring.cloud.stream.bindings.storeInventoryBySKU.binder=test2"})
public class StoreSkuInventoryRepositoryIntegrationTest {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private StoreSkuInventorySyncRepository skuInventorySyncRepository;

    private StoreSkuInventoryDocumentLoader storeSkuInventoryDocumentLoader;

    @Before
    public void setUp() {
        storeSkuInventoryDocumentLoader = new StoreSkuInventoryDocumentLoader(this.client);
    }

    @Test
    public void shouldCreateOrUpdateStoreSkuInventory() throws InterruptedException, IOException {
        StoreSkuInventoryDocument storeSkuInventoryDocument = new StoreSkuInventoryDocumentBuilder()
                .withStoreNumber("OLD_STORE_NUMBER")
                .build();

        skuInventorySyncRepository.createOrUpdateStoreSkuInventory(storeSkuInventoryDocument);

        assertThat(storeSkuInventoryDocumentLoader.findById(storeSkuInventoryDocument.getId()).get("storeNumber"), is("OLD_STORE_NUMBER"));

        storeSkuInventoryDocument.setStoreNumber("NEW_STORE_NUMBER");

        skuInventorySyncRepository.createOrUpdateStoreSkuInventory(storeSkuInventoryDocument);

        assertThat(storeSkuInventoryDocumentLoader.findById(storeSkuInventoryDocument.getId()).get("storeNumber"), is("NEW_STORE_NUMBER"));
    }

    @Test
    public void shouldSaveNullStoreSkuInventoryFieldsInElasticSearch() throws InterruptedException, IOException {
        StoreSkuInventoryDocument storeSkuInventoryDocument = new StoreSkuInventoryDocumentBuilder()
                .withInventoryLevelCode(null).build();

        skuInventorySyncRepository.createOrUpdateStoreSkuInventory(storeSkuInventoryDocument);

        assertThat(storeSkuInventoryDocumentLoader.findById(storeSkuInventoryDocument.getId()).get("inventoryLevelCode"), is(nullValue()));
    }
}

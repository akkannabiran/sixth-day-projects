package com.sixthday.store;

import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryDocument;
import com.sixthday.store.repository.ElasticsearchRepository;
import com.sixthday.store.setup.StoreDocumentPreloader;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {StoreLocatorServiceApplication.class},
  properties = {"spring.cache.type=none", "spring.cloud.stream.bindings.output.binder=test","sixthday-store-sub.products-foundation-mode=false","spring.cloud.stream.bindings.storeInventoryBySKU.binder=test2"})
public class SkuSearchIntegrationTest {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private ElasticsearchRepository elasticsearchRepository;

    private StoreDocumentPreloader storeDocumentBuilder;

    @Before
    public void setUp() throws Exception {
        storeDocumentBuilder = new StoreDocumentPreloader(client);
        storeDocumentBuilder.addTestStoresToElasticSearch();
        storeDocumentBuilder.addTestSkuToElasticSearch();
    }

    @After
    public void tearDown() {
            storeDocumentBuilder.removeTestStoresFromElasticSearch();
            storeDocumentBuilder.removeTestSkuFromElasticSearch();


    }

    @Test
    public void shouldGetStoreSkuDocumentsGivenOneStoreNumberAndSkuId() throws Exception {
        List<String> storeNumbersToSearch = new ArrayList<>();
        storeNumbersToSearch.add("99888");

        List<StoreSkuInventoryDocument> storesSkus = elasticsearchRepository.getAllStoresSkus(storeNumbersToSearch,"sku118570888");

        Assert.assertThat(storesSkus.size(), is(1));
        Assert.assertThat(storesSkus.get(0).getStoreNumber(), is("99888"));
    }

    @Test
    public void shouldGetStoreSkuDocumentsGivenListOfStoreNumbersAndSkuId() throws Exception {
        List<String> storeNumbersToSearch = new ArrayList<>();
        storeNumbersToSearch.add("99777");
        storeNumbersToSearch.add("99666");

        List<StoreSkuInventoryDocument> storesSkus = elasticsearchRepository.getAllStoresSkus(storeNumbersToSearch,"sku118570889");

        Assert.assertThat(storesSkus.size(), is(2));
        Assert.assertTrue(storesSkus.stream().anyMatch(p->p.getStoreNumber().equals("99777")));
        Assert.assertTrue(storesSkus.stream().anyMatch(p->p.getStoreNumber().equals("99666")));
    }

    @Test
    public void shouldGetEmptyStoreSkuDocsWhenStoresNotFoundForSkuId() throws Exception {
        List<String> storeNumbersToSearch = new ArrayList<>();
        storeNumbersToSearch.add("invalidStore");

        List<StoreSkuInventoryDocument> storesSkus = elasticsearchRepository.getAllStoresSkus(storeNumbersToSearch,"sku118570889");

        Assert.assertThat(storesSkus.size(), is(0));
    }
}

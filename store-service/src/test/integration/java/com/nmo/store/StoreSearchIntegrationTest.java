package com.sixthday.store;

import com.sixthday.store.data.StoreDocumentBuilder;
import com.sixthday.store.exceptions.DocumentNotFoundException;
import com.sixthday.store.models.storeindex.StoreDocument;
import com.sixthday.store.repository.ElasticsearchRepository;
import com.sixthday.store.setup.StoreDocumentLoader;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;

import static com.sixthday.store.config.Constants.Filters.IS_DISPLAYABLE;
import static com.sixthday.store.config.Constants.Filters.IS_ELIGIBLE_FOR_BOPS;
import static com.sixthday.store.data.StoreDocumentBuilder.randomStoreNumber;
import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {StoreLocatorServiceApplication.class},
  properties = {"spring.cache.type=none", "spring.cloud.stream.bindings.output.binder=test", "sixthday-store-sub.products-foundation-mode=false","spring.cloud.stream.bindings.storeInventoryBySKU.binder=test2"})
public class StoreSearchIntegrationTest {

    private static final String STORE_ID_1 = UUID.randomUUID().toString();
    private static final String STORE_ID_2 = UUID.randomUUID().toString();

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private ElasticsearchRepository elasticsearchRepository;

    private StoreDocumentLoader storeDocumentLoader;

    @Before
    public void setUp() {
        storeDocumentLoader = new StoreDocumentLoader(client);
    }

    @After
    public void tearDown() {
        storeDocumentLoader.clear();
    }

    @Test
    public void shouldReturnStoreDocumentWithDisplayableFlagEqualToTrue() {
        StoreDocument storeDocumentWithDisplayFlagOn = new StoreDocumentBuilder().withId(STORE_ID_1).withDisplayable(true).build();
        storeDocumentLoader.add(storeDocumentWithDisplayFlagOn).save();

        List<StoreDocument> storeDocuments = elasticsearchRepository.getStoresById(STORE_ID_1);

        assertThat(storeDocuments.get(0).getId(), is(STORE_ID_1));
    }

    @Test(expected = DocumentNotFoundException.class)
    public void shouldNotReturnStoreDocumentWhenDisplayableIsFalse() {
        StoreDocument storeDocumentWithDisplayFlagOff = new StoreDocumentBuilder().withId(STORE_ID_2).withDisplayable(false).build();
        storeDocumentLoader.add(storeDocumentWithDisplayFlagOff).save();

        elasticsearchRepository.getStoresById(STORE_ID_2);
    }

    @Test
    public void shouldReturnOnlyStoreDocumentsEligibleForBOPSGivenDisplayFlagIsTrue() {
        String storeNumberOne = randomStoreNumber();
        String storeNumberTwo = randomStoreNumber();
        List<String> storeNumbersToSearch = asList(storeNumberOne, storeNumberTwo);
        StoreDocument displayableStoreDocumentEligibleForBops = new StoreDocumentBuilder().withStoreNumber(storeNumberOne)
                .withEligibleForBOPS(true).withDisplayable(true).build();
        StoreDocument displayableStoreDocumentInEligibleForBops = new StoreDocumentBuilder().withStoreNumber(storeNumberTwo)
                .withEligibleForBOPS(false).withDisplayable(true).build();
        storeDocumentLoader.add(displayableStoreDocumentEligibleForBops).add(displayableStoreDocumentInEligibleForBops).save();

        List<StoreDocument> storeDocuments = elasticsearchRepository.filterStoresForStoreNumbers(storeNumbersToSearch, IS_ELIGIBLE_FOR_BOPS);

        assertThat(storeDocuments.size(), is(1));
        assertTrue(storeDocuments.stream().anyMatch(p->p.getStoreNumber().equals(storeNumberOne)));
    }

    @Test
    public void shouldNotReturnAnyStoreDocumentsWhenDisplayFlagFalse() {
        String storeNumberOne = randomStoreNumber();
        String storeNumberTwo = randomStoreNumber();
        List<String> storeNumbersToSearch = asList(storeNumberOne, storeNumberTwo);
        StoreDocument hiddenStoreDocumentEligibleForBops = new StoreDocumentBuilder().withStoreNumber(storeNumberOne)
                .withEligibleForBOPS(true).withDisplayable(false).build();
        StoreDocument hiddenStoreDocumentInEligibleForBops = new StoreDocumentBuilder().withStoreNumber(storeNumberTwo)
                .withEligibleForBOPS(false).withDisplayable(false).build();
        storeDocumentLoader.add(hiddenStoreDocumentEligibleForBops).add(hiddenStoreDocumentInEligibleForBops).save();

        List<StoreDocument> storeDocuments = elasticsearchRepository.filterStoresForStoreNumbers(storeNumbersToSearch, IS_ELIGIBLE_FOR_BOPS);

        assertThat(storeDocuments.isEmpty(), is(true));
    }

    @Test
    public void shouldReturnOnlyStoreDocumentsGivenListOfStoreNumbers() {
        String storeNumberOne = randomStoreNumber();
        String storeNumberTwo = randomStoreNumber();
        StoreDocument displayableStoreDocument = new StoreDocumentBuilder().withStoreNumber(storeNumberOne).withDisplayable(true).build();
        StoreDocument hiddenStoreDocument = new StoreDocumentBuilder().withStoreNumber(storeNumberTwo).withDisplayable(false).build();
        storeDocumentLoader.add(hiddenStoreDocument).add(displayableStoreDocument).save();

        List<StoreDocument> storeDocuments = elasticsearchRepository.filterStoresForStoreNumbers(asList(storeNumberOne, storeNumberTwo), IS_DISPLAYABLE);

        assertThat(storeDocuments.size(), is(1));
        assertThat(storeDocuments.get(0).getStoreNumber(),is(storeNumberOne));
    }
}

package com.sixthday.store.services;

import com.sixthday.store.data.*;
import com.sixthday.store.exceptions.DocumentRetrievalException;
import com.sixthday.store.models.*;
import com.sixthday.store.models.sixthdayStore.SkuAvailability;
import com.sixthday.store.models.storeindex.StoreDocument;
import com.sixthday.store.models.storeindex.StoreEventDocument;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryDocument;
import com.sixthday.store.repository.ElasticsearchRepository;
import com.sixthday.store.repository.GotWWWRepository;
import com.sixthday.store.repository.dynamodb.DynamoDBStoreInventoryRepository;
import com.toggler.core.utils.FeatureToggleRepository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.*;

import static com.sixthday.store.config.Constants.Filters.IS_ELIGIBLE_FOR_BOPS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StoreSearchServiceTest {

    private static final String BRAND_CODE = "BRAND_CODE";
    private static final String SKU_ID = "SKU_ID";
    private static final Integer REQUESTED_QUANTITY = 1;
    private static final int RADIUS = 100;

    @Mock
    private GotWWWRepository gotWWWRepository;

    @Mock
    private ElasticsearchRepository elasticsearchRepository;

    @Mock
    private DynamoDBStoreInventoryRepository dynamoDBStoreInventoryRepository;

    @InjectMocks
    private StoreSearchService storeSearchService;

    @Rule
    public FeatureToggleRepository featureToggleRepositoryRule = new FeatureToggleRepository();
    
    @Test
    public void shouldCallGotWWWWithGivenParameters() throws Exception {
        StoreSearchLocation storeSearchLocation = new StoreSearchLocation("someAddress");

        storeSearchService.getStores(BRAND_CODE, storeSearchLocation, Optional.of(RADIUS));

        verify(gotWWWRepository).getStores(BRAND_CODE, storeSearchLocation, Optional.of(RADIUS));
    }

    @Test
    public void shouldCallElasticsearchRepositoryWithStoreNumbersReturnedByGotWWW() throws Exception {
        StoreSearchLocation storeSearchLocation = new StoreSearchLocation("someAddress");

        List<String> storeNumbers = Arrays.asList("01", "02");
        when(gotWWWRepository.getStores(BRAND_CODE, storeSearchLocation, Optional.of(RADIUS))).thenReturn(storeNumbers);

        storeSearchService.getStoresFromElasticSearch(BRAND_CODE, storeSearchLocation, Optional.of(RADIUS), SKU_ID, REQUESTED_QUANTITY);

        verify(elasticsearchRepository).filterStoresForStoreNumbers(storeNumbers, IS_ELIGIBLE_FOR_BOPS);
    }

    @Test
    public void shouldReturnsixthdayStoreForGivenParameters() throws Exception {
        StoreSearchLocation storeSearchLocation = new StoreSearchLocation("someAddress");
        List<String> storeNumbers = Arrays.asList("01", "02");
        when(gotWWWRepository.getStores(BRAND_CODE, storeSearchLocation, Optional.of(RADIUS))).thenReturn(storeNumbers);

        ArrayList<StoreEventDocument> storeEvents = new ArrayList<>();
        storeEvents.add(new StoreEventDocument("storeEventId", "storeName", "storeEventTypeId", "storeDescription", LocalDate.now(), LocalDate.now(), "eventSchedule", "timeDuration"));

        StoreDocument storeDocument = new StoreDocument("storeId", "1", "storeName", "addressLine1", "addressLine2", "city", "state", "zipCode", "phoneNumber", "storeHours", "description", true, true, storeEvents);
        List<StoreDocument> storeDocuments = Collections.singletonList(storeDocument);


        when(elasticsearchRepository.filterStoresForStoreNumbers(storeNumbers, IS_ELIGIBLE_FOR_BOPS)).thenReturn(storeDocuments);

        List<sixthdayStore> sixthdayStores = storeSearchService.getStoresFromElasticSearch(BRAND_CODE, storeSearchLocation, Optional.of(RADIUS), SKU_ID, REQUESTED_QUANTITY);

        assertThat(sixthdayStores.size(), is(1));
        assertThat(sixthdayStores.get(0).getStoreNumber(), is(storeDocument.getStoreNumber()));
        assertThat(sixthdayStores.get(0).getStoreId(), is(storeDocument.getId()));
        assertThat(sixthdayStores.get(0).getName(), is(storeDocument.getStoreName()));
        assertThat(sixthdayStores.get(0).getImage(), is("/category/stores/" + storeDocument.getId() + "/images/r_main.jpg"));
        assertThat(sixthdayStores.get(0).getAddressLine1(), is(storeDocument.getAddressLine1()));
        assertThat(sixthdayStores.get(0).getAddressLine2(), is(storeDocument.getAddressLine2()));
        assertThat(sixthdayStores.get(0).getCity(), is(storeDocument.getCity()));
        assertThat(sixthdayStores.get(0).getPhoneNumber(), is(storeDocument.getPhoneNumber()));
        assertThat(sixthdayStores.get(0).getStoreHours(), is(storeDocument.getStoreHours()));

        assertThat(sixthdayStores.get(0).getEvents().size(), is(storeDocument.getEvents().size()));
        assertThat(sixthdayStores.get(0).getEvents().get(0).getName(), is(storeDocument.getEvents().get(0).getEventName()));
        assertThat(sixthdayStores.get(0).getEvents().get(0).getDescription(), is(storeDocument.getEvents().get(0).getEventDescription()));
        assertThat(sixthdayStores.get(0).getEvents().get(0).getEventTimeDuration(), is(storeDocument.getEvents().get(0).getEventDuration()));

    }

    @Test
    public void shouldReturnsixthdayStoresWhenSkuIdAndUserRequestedQuantityIsGiven() throws Exception {
        StoreSearchLocation storeSearchLocation = new StoreSearchLocation("someAddress");
        List<String> storeNumbers = Arrays.asList("01", "02", "03");
        when(gotWWWRepository.getStores(BRAND_CODE, storeSearchLocation, Optional.of(RADIUS))).thenReturn(storeNumbers);

        StoreSkuInventoryDocument storeSkuInventoryDocument1 = new StoreSkuInventoryDocumentBuilder()
                .withStoreNumber("01").withInventoryLevelCode("1").withQuantity(1).build();
        StoreSkuInventoryDocument storeSkuInventoryDocument2 =new StoreSkuInventoryDocumentBuilder()
                .withStoreNumber("02").withInventoryLevelCode("1").withQuantity(2).build();
        List<StoreSkuInventoryDocument> storeSkuInventoryDocuments = new ArrayList<>();
        storeSkuInventoryDocuments.add(storeSkuInventoryDocument1);
        storeSkuInventoryDocuments.add(storeSkuInventoryDocument2);

        when(elasticsearchRepository.getAllStoresSkus(storeNumbers, SKU_ID)).thenReturn(storeSkuInventoryDocuments);

        StoreDocument storeDocument1 = new StoreDocumentBuilder().withStoreNumber("01").build();
        StoreDocument storeDocument2 = new StoreDocumentBuilder().withStoreNumber("02").build();
        StoreDocument storeDocument3 = new StoreDocumentBuilder().withStoreNumber("03").build();

        List<StoreDocument> storeDocuments = new ArrayList<>();
        storeDocuments.add(storeDocument1);
        storeDocuments.add(storeDocument2);
        storeDocuments.add(storeDocument3);

        when(elasticsearchRepository.filterStoresForStoreNumbers(storeNumbers,IS_ELIGIBLE_FOR_BOPS)).thenReturn(storeDocuments);
        featureToggleRepositoryRule.disable("READ_STORE_SKUS");
        List<sixthdayStore> sixthdayStores = storeSearchService.getStoresFromElasticSearch(BRAND_CODE, storeSearchLocation, Optional.of(RADIUS), SKU_ID, REQUESTED_QUANTITY);

        assertThat(sixthdayStores.size(), is(3));

        assertThat(sixthdayStores.get(0).getStoreNumber(), is("01"));
        assertThat(sixthdayStores.get(1).getStoreNumber(), is("02"));
        assertThat(sixthdayStores.get(2).getStoreNumber(), is("03"));

        SkuAvailabilityInfo skuAvailabilityInfo1 = new SkuAvailabilityInfo(storeSkuInventoryDocument1, 1);
        SkuAvailabilityInfo skuAvailabilityInfo2 = new SkuAvailabilityInfo(storeSkuInventoryDocument2, 2);
        SkuAvailability skuAvailability1 = new SkuAvailability(skuAvailabilityInfo1.getAvailabilityStatus(), skuAvailabilityInfo1.isInventoryAvailable(), skuAvailabilityInfo1.getAddToCartMessage());
        SkuAvailability skuAvailability2 = new SkuAvailability(skuAvailabilityInfo2.getAvailabilityStatus(), skuAvailabilityInfo2.isInventoryAvailable(), skuAvailabilityInfo2.getAddToCartMessage());

        assertThat(sixthdayStores.get(0).getSkuAvailability(), is(skuAvailability1));
        assertThat(sixthdayStores.get(0).getSkuAvailability(), is(skuAvailability2));
        assertThat(sixthdayStores.get(2).getSkuAvailability(), is(SkuAvailability.getDefault()));

    }

    @Test
    public void shouldSortsixthdayStoresByDistance() throws Exception {
        StoreSearchLocation storeSearchLocation = new StoreSearchLocation("someAddress");
        List<String> storeNumbers = Arrays.asList("01", "02", "03");
        when(gotWWWRepository.getStores(BRAND_CODE, storeSearchLocation, Optional.of(RADIUS))).thenReturn(storeNumbers);

        StoreDocument storeDocument1 = new StoreDocumentBuilder().withStoreNumber("01").build();
        StoreDocument storeDocument2 = new StoreDocumentBuilder().withStoreNumber("02").build();
        StoreDocument storeDocument3 = new StoreDocumentBuilder().withStoreNumber("03").build();

        List<StoreDocument> storeDocuments = new ArrayList<>();
        storeDocuments.add(storeDocument2);
        storeDocuments.add(storeDocument1);
        storeDocuments.add(storeDocument3);

        when(elasticsearchRepository.filterStoresForStoreNumbers(storeNumbers, IS_ELIGIBLE_FOR_BOPS)).thenReturn(storeDocuments);

        List<sixthdayStore> sixthdayStores = storeSearchService.getStoresFromElasticSearch(BRAND_CODE, storeSearchLocation, Optional.of(RADIUS), SKU_ID, REQUESTED_QUANTITY);

        assertThat(sixthdayStores.get(0).getStoreNumber(), is("01"));
        assertThat(sixthdayStores.get(1).getStoreNumber(), is("02"));
        assertThat(sixthdayStores.get(2).getStoreNumber(), is("03"));
    }

    @Test
    public void shouldReturnFirstStoreWhenStoreIsAvailable() throws Exception {
        String storeNumbers = "100/DT";
        ArrayList<StoreEventDocument> storeEventDocuments = new ArrayList<>();
        StoreDocument storeDocument = new StoreDocument("storeId", "1", "storeName", "addressLine1", "addressLine2", "city", "state", "zipCode", "phoneNumber", "storeHours", "description", true, true, storeEventDocuments);
        when(elasticsearchRepository.getStoresById(storeNumbers)).thenReturn(Arrays.asList(storeDocument));

        List<sixthdayStore> sixthdayStore = storeSearchService.getStores("100/DT");

        assertThat(sixthdayStore.get(0).getName(), is(storeDocument.getStoreName()));
        assertThat(sixthdayStore.get(0).getStoreId(), is(storeDocument.getId()));
    }

    @Test
    public void shouldReturnFirstStoreWhenStoreIsAvailableEvenWithNoEvents() throws Exception {
        String storeNumbers = "100/DT";
        ArrayList<StoreEventDocument> storeEventDocuments = new ArrayList<>();
        StoreEventDocument storeEventDocument = new StoreEventDocument(null, null, null, null, null, null, null, null);
        storeEventDocuments.add(storeEventDocument);

        StoreDocument storeDocument = new StoreDocument("storeId", "1", "storeName", "addressLine1", "addressLine2", "city", "state", "zipCode", "phoneNumber", "storeHours", "description", true, true, storeEventDocuments);
        when(elasticsearchRepository.getStoresById(storeNumbers)).thenReturn(Arrays.asList(storeDocument));

        List<sixthdayStore> sixthdayStore = storeSearchService.getStores("100/DT");

        assertThat(sixthdayStore.get(0).getName(), is(storeDocument.getStoreName()));
        assertThat(sixthdayStore.get(0).getStoreId(), is(storeDocument.getId()));
    }

    @Test(expected = DocumentRetrievalException.class)
    public void shouldThrowDocumentRetrievalExceptionWhenDocumentRetrievalFails() throws Exception {
        StoreSearchLocation storeSearchLocation = new StoreSearchLocation("someAddress");
        List<String> storeNumbers = Arrays.asList("01", "02", "03");
        when(gotWWWRepository.getStores(BRAND_CODE, storeSearchLocation, Optional.of(RADIUS))).thenReturn(storeNumbers);
        when(elasticsearchRepository.filterStoresForStoreNumbers(any(), any(String.class))).thenThrow(new NullPointerException("error"));
        storeSearchService.getStoresFromElasticSearch(BRAND_CODE,storeSearchLocation,Optional.of(RADIUS),"test",2);
    }

    @Test
    public void shouldReturnEmptyStoreInventoryWhenNoSkuMatches() {
        when(dynamoDBStoreInventoryRepository.getSKUsInventory(new HashSet<>(Arrays.asList("sku1", "sku2")))).thenReturn(Collections.emptyList());
        StoreSkuInventory storeSkuInventory = storeSearchService.getSKUsInventory("storeId", "sku1,sku2");
        assertNotNull(storeSkuInventory);
        assertNull(storeSkuInventory.getStore());
        assertThat(storeSkuInventory.getSkuInventories().size(), is(0));
    }

    @Test
    public void shouldReturnNonEmptyStoreInventoryWhenNoSkuMatches() {
        StoreInventoryBySKUDocumentBuilder storeInventoryBySKUDocumentBuilder = new StoreInventoryBySKUDocumentBuilder();
        StoreInventoryItemBuilder storeInventoryItemBuilder = new StoreInventoryItemBuilder();

        List<StoreInventoryItem> storeInventoryItems1 = Arrays.asList(
                storeInventoryItemBuilder.withAll("s1", "s1/1", "l1", "l1", 1, 1).build(),
                storeInventoryItemBuilder.withAll("s2", "s2/2", "l2", "l2", 2, 2).build(),
                storeInventoryItemBuilder.withAll("s3", "s3/3", "l3", "l3", 3, 3).build()
        );
        StoreInventoryBySKUDocument storeInventoryBySKUDocument1 = storeInventoryBySKUDocumentBuilder.withSkuId("sku1").withStoreInventoryItems(storeInventoryItems1).build();

        List<StoreInventoryItem> storeInventoryItems2 = Arrays.asList(
                storeInventoryItemBuilder.withAll("s2", "s2/2", "l2", "l2", 2, 2).build(),
                storeInventoryItemBuilder.withAll("s3", "s3/3", "l3", "l3", 3, 3).build()
        );
        StoreInventoryBySKUDocument storeInventoryBySKUDocument2 = storeInventoryBySKUDocumentBuilder.withSkuId("sku2").withStoreInventoryItems(storeInventoryItems2).build();

        List<StoreInventoryItem> storeInventoryItems3 = Arrays.asList(
                storeInventoryItemBuilder.withAll("s1", "s1/1", "l1", "l1", 1, 1).build(),
                storeInventoryItemBuilder.withAll("s2", "s2/2", "l2", "l2", 2, 2).build(),
                storeInventoryItemBuilder.withAll("s3", "s3/3", "l3", "l3", 3, 3).build()
        );
        StoreInventoryBySKUDocument storeInventoryBySKUDocument3 = storeInventoryBySKUDocumentBuilder.withSkuId("sku3").withStoreInventoryItems(storeInventoryItems3).build();

        List<StoreInventoryBySKUDocument> storeInventoryBySKUDocuments = Arrays.asList(storeInventoryBySKUDocument1, storeInventoryBySKUDocument2, storeInventoryBySKUDocument3);

        when(dynamoDBStoreInventoryRepository.getSKUsInventory(new HashSet<>(Arrays.asList("sku1", "sku2", "sku3")))).thenReturn(storeInventoryBySKUDocuments);
        StoreSkuInventory storeInventoryItems = storeSearchService.getSKUsInventory("s1", "sku1,sku2,sku3");
        assertNotNull(storeInventoryItems);
        assertNotNull(storeInventoryItems.getStore());
        assertThat(storeInventoryItems.getStore().getStoreId(), is("s1/1"));
        assertThat(storeInventoryItems.getStore().getLocationNumber(), is("l1"));
        assertThat(storeInventoryItems.getStore().getStoreNumber(), is("s1"));
        assertNotNull(storeInventoryItems.getSkuInventories());
        assertThat(storeInventoryItems.getSkuInventories().size(), is(2));
        assertThat(storeInventoryItems.getSkuInventories().get(0).getBopsQuantity(), is(1));
        assertThat(storeInventoryItems.getSkuInventories().get(0).getInventoryLevel(), is("l1"));
        assertThat(storeInventoryItems.getSkuInventories().get(0).getQuantity(), is(1));
        assertThat(storeInventoryItems.getSkuInventories().get(0).getSkuId(), is("sku1"));
        assertThat(storeInventoryItems.getSkuInventories().get(1).getBopsQuantity(), is(1));
        assertThat(storeInventoryItems.getSkuInventories().get(1).getInventoryLevel(), is("l1"));
        assertThat(storeInventoryItems.getSkuInventories().get(1).getQuantity(), is(1));
        assertThat(storeInventoryItems.getSkuInventories().get(1).getSkuId(), is("sku3"));
    }
}

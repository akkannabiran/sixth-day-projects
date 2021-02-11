package com.sixthday.store.services;

import static com.sixthday.store.config.Constants.Filters.IS_ELIGIBLE_FOR_BOPS;
import static com.sixthday.store.models.SkuAvailabilityInfo.AVAILABLE;
import static com.sixthday.store.models.SkuAvailabilityInfo.AVAILABLE_FOR_PICKUP_TODAY;
import static com.sixthday.store.models.SkuAvailabilityInfo.LIMITED_AVAILABILITY;
import static com.sixthday.store.models.SkuAvailabilityInfo.LIMITED_STOCK;
import static com.sixthday.store.models.SkuAvailabilityInfo.NOT_AVAILABLE;
import static com.sixthday.store.models.SkuAvailabilityInfo.NOT_AVAILABLE_TODAY;
import static com.sixthday.store.models.SkuAvailabilityInfo.PICK_UP_IN_TWO_THREE_DAYS;
import static com.sixthday.store.models.SkuAvailabilityInfo.PICK_UP_TODAY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.amazonaws.services.dynamodbv2.model.InternalServerErrorException;
import com.amazonaws.services.dynamodbv2.model.LimitExceededException;
import com.sixthday.store.data.StoreDocumentBuilder;
import com.sixthday.store.models.sixthdayStore;
import com.sixthday.store.models.sixthdayStore.SkuAvailability;
import com.sixthday.store.models.StoreSearchLocation;
import com.sixthday.store.models.storeindex.StoreDocument;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryDocument;
import com.sixthday.store.repository.ElasticsearchRepository;
import com.sixthday.store.repository.GotWWWRepository;
import com.sixthday.store.repository.dynamodb.DynamoDBStoreInventoryRepository;
import com.toggler.core.utils.FeatureToggleRepository;

@RunWith(MockitoJUnitRunner.class)
public class StoreSearchServiceUsingDynamoDBTest {

    private static final String BRAND_CODE = "BRAND_CODE";
    private static final String SKU_ID = "SKU_ID";
    private static final Integer REQUESTED_QUANTITY = 100;
    private static final int RADIUS = 100;

    @Mock
    private GotWWWRepository gotWWWRepository;

    @Mock
    private ElasticsearchRepository elasticsearchRepository;

    @Mock
    private DynamoDBStoreInventoryRepository dynamoStoreInventoryRepository;
    
    @InjectMocks
    private StoreSearchService storeSearchService;

    @Rule
    public FeatureToggleRepository featureToggleRepositoryRule = new FeatureToggleRepository();
    
    private StoreSearchLocation  storeSearchLocation;
    
    @Before
    public void setup(){
      storeSearchLocation = new StoreSearchLocation("someAddress");
    }

    private void mockupStoresAndStoreInvenotoriesForAvailabilityTests(String inventoryLevel) {
    	 List<String> storeNumbers = Arrays.asList("01", "02", "03", "These", "are", "not", "considered");
         when(gotWWWRepository.getStores(BRAND_CODE, storeSearchLocation, Optional.of(RADIUS))).thenReturn(storeNumbers);
         StoreSkuInventoryDocument inventoryDoc1 = new StoreSkuInventoryDocument("11", "sku1", "01", "01/L1", "L1", inventoryLevel, 100, 100, null);
         StoreSkuInventoryDocument inventoryDoc2 = new StoreSkuInventoryDocument("21", "sku1", "02", "02/L2", "L2", inventoryLevel, 200, 200, null);
         StoreSkuInventoryDocument inventoryDoc3 = new StoreSkuInventoryDocument("31", "sku1", "03", "03/L3", "L3", inventoryLevel, 10, 10, null);
         StoreSkuInventoryDocument noMatch1 = new StoreSkuInventoryDocument("T1", "sku1", "A99", "A99/L3", "L3", inventoryLevel, 10000, 910, null);
         StoreSkuInventoryDocument noMatch2 = new StoreSkuInventoryDocument("TD1", "sku1", "B99", "B99/L3", "L3", inventoryLevel, 10000, 1010, null);
         List<StoreSkuInventoryDocument> storeInventoryDocs = Arrays.asList(inventoryDoc1, inventoryDoc2, inventoryDoc3, noMatch1, noMatch2);

         when(dynamoStoreInventoryRepository.getAllStoresSkus(storeNumbers, SKU_ID)).thenReturn(storeInventoryDocs);
         StoreDocumentBuilder builder = new StoreDocumentBuilder();
         List<StoreDocument> storeDocuments = Arrays.asList(builder.withStoreNumber("01").build(),
        		 builder.withStoreNumber("02").build(), builder.withStoreNumber("03").build());
         
         when(elasticsearchRepository.filterStoresForStoreNumbers(storeNumbers, IS_ELIGIBLE_FOR_BOPS)).thenReturn(storeDocuments);
    }
    
    @Test
    public void shouldReturnAllsixthdayStoresWhenSkuIdAndUserRequestedQuantityIsGivenAndInventoryLevelIsNotAvailable() throws Exception {
       
    	mockupStoresAndStoreInvenotoriesForAvailabilityTests(NOT_AVAILABLE);
        
        List<sixthdayStore> sixthdayStores = storeSearchService.getStores(BRAND_CODE, storeSearchLocation, Optional.of(RADIUS), SKU_ID, REQUESTED_QUANTITY);

        assertThat(sixthdayStores.size(), is(3));

        assertThat(sixthdayStores.get(0).getStoreNumber(), is("01"));
        assertThat(sixthdayStores.get(1).getStoreNumber(), is("02"));
        assertThat(sixthdayStores.get(2).getStoreNumber(), is("03"));

        assertSkuAvailability(sixthdayStores.get(0).getSkuAvailability(), NOT_AVAILABLE_TODAY, PICK_UP_IN_TWO_THREE_DAYS, false);
        assertSkuAvailability(sixthdayStores.get(1).getSkuAvailability(), NOT_AVAILABLE_TODAY, PICK_UP_IN_TWO_THREE_DAYS, false);
        assertSkuAvailability(sixthdayStores.get(2).getSkuAvailability(), NOT_AVAILABLE_TODAY, PICK_UP_IN_TWO_THREE_DAYS, false);
    }
    
    @Test
    public void shouldReturnAllsixthdayStoresWhenSkuIdAndUserRequestedQuantityIsGivenAndInventoryLevelIsAvailable() throws Exception {
    	mockupStoresAndStoreInvenotoriesForAvailabilityTests(AVAILABLE);
        
        List<sixthdayStore> sixthdayStores = storeSearchService.getStores(BRAND_CODE, storeSearchLocation, Optional.of(RADIUS), SKU_ID, REQUESTED_QUANTITY);

        assertThat(sixthdayStores.size(), is(3));
        assertThat(sixthdayStores.get(0).getStoreNumber(), is("01"));
        assertThat(sixthdayStores.get(1).getStoreNumber(), is("02"));
        assertThat(sixthdayStores.get(2).getStoreNumber(), is("03"));

        assertSkuAvailability(sixthdayStores.get(0).getSkuAvailability(), AVAILABLE_FOR_PICKUP_TODAY, PICK_UP_TODAY, true);
        assertSkuAvailability(sixthdayStores.get(1).getSkuAvailability(), AVAILABLE_FOR_PICKUP_TODAY, PICK_UP_TODAY, true);
        assertSkuAvailability(sixthdayStores.get(2).getSkuAvailability(), NOT_AVAILABLE_TODAY, PICK_UP_IN_TWO_THREE_DAYS, false);
    }
    
    @Test
    public void shouldReturnAllsixthdayStoresWhenSkuIdAndUserRequestedQuantityIsGivenAndInventoryLevelIsLimitedAvailability() throws Exception {
    	mockupStoresAndStoreInvenotoriesForAvailabilityTests(LIMITED_AVAILABILITY);

    	List<sixthdayStore> sixthdayStores = storeSearchService.getStores(BRAND_CODE, storeSearchLocation, Optional.of(RADIUS), SKU_ID, REQUESTED_QUANTITY);

        assertThat(sixthdayStores.size(), is(3));
        assertThat(sixthdayStores.get(0).getStoreNumber(), is("01"));
        assertThat(sixthdayStores.get(1).getStoreNumber(), is("02"));
        assertThat(sixthdayStores.get(2).getStoreNumber(), is("03"));

        assertSkuAvailability(sixthdayStores.get(0).getSkuAvailability(), LIMITED_STOCK, PICK_UP_TODAY, true);
        assertSkuAvailability(sixthdayStores.get(1).getSkuAvailability(), LIMITED_STOCK, PICK_UP_TODAY, true);
        assertSkuAvailability(sixthdayStores.get(2).getSkuAvailability(), NOT_AVAILABLE_TODAY, PICK_UP_IN_TWO_THREE_DAYS, false);
    }
    
    @Test
    public void shouldReturnAllsixthdayStoresWhenSkuIdAndUserRequestedQuantityIsGivenAndInventoryLevelIsUnknown() throws Exception {
    	mockupStoresAndStoreInvenotoriesForAvailabilityTests("3");
        
        List<sixthdayStore> sixthdayStores = storeSearchService.getStores(BRAND_CODE, storeSearchLocation, Optional.of(RADIUS), SKU_ID, REQUESTED_QUANTITY);

        assertThat(sixthdayStores.size(), is(3));
        assertThat(sixthdayStores.get(0).getStoreNumber(), is("01"));
        assertThat(sixthdayStores.get(1).getStoreNumber(), is("02"));
        assertThat(sixthdayStores.get(2).getStoreNumber(), is("03"));

        assertSkuAvailability(sixthdayStores.get(0).getSkuAvailability(), NOT_AVAILABLE_TODAY, PICK_UP_IN_TWO_THREE_DAYS, false);
        assertSkuAvailability(sixthdayStores.get(1).getSkuAvailability(), NOT_AVAILABLE_TODAY, PICK_UP_IN_TWO_THREE_DAYS, false);
        assertSkuAvailability(sixthdayStores.get(2).getSkuAvailability(), NOT_AVAILABLE_TODAY, PICK_UP_IN_TWO_THREE_DAYS, false);
    }
    
    @Test
    public void shouldReturnsixthdayStoresWithNotAvailableStatusWhenSkuDoesntHaveDataInDynamoDB() throws Exception {
    	List<String> storeNumbers = Arrays.asList("01", "02");
		when(gotWWWRepository.getStores(anyString(), any(), any())).thenReturn(storeNumbers);
    	StoreDocumentBuilder builder = new StoreDocumentBuilder();
        List<StoreDocument> storeDocuments = Arrays.asList(builder.withStoreNumber("01").build(), builder.withStoreNumber("02").build());
        when(elasticsearchRepository.filterStoresForStoreNumbers(storeNumbers, IS_ELIGIBLE_FOR_BOPS)).thenReturn(storeDocuments);
    	when(dynamoStoreInventoryRepository.getAllStoresSkus(any(), any())).thenReturn(Collections.emptyList());        

    	List<sixthdayStore> sixthdayStores = storeSearchService.getStores(BRAND_CODE, storeSearchLocation, Optional.of(RADIUS), SKU_ID, REQUESTED_QUANTITY);
        
    	assertThat(sixthdayStores.size(), is(2));
        assertSkuAvailability(sixthdayStores.get(0).getSkuAvailability(), NOT_AVAILABLE_TODAY, PICK_UP_IN_TWO_THREE_DAYS, false);
        assertSkuAvailability(sixthdayStores.get(1).getSkuAvailability(), NOT_AVAILABLE_TODAY, PICK_UP_IN_TWO_THREE_DAYS, false);
    }
    @Test
    public void shouldReturnEmptysixthdayStoresWhenElasticSearchDoesntHaveStoreInfo() throws Exception {
    	when(gotWWWRepository.getStores(anyString(), any(), any())).thenReturn(Arrays.asList("INVALID1", "INVALID2"));
    	        
        List<sixthdayStore> sixthdayStores = storeSearchService.getStores(BRAND_CODE, storeSearchLocation, Optional.of(RADIUS), SKU_ID, REQUESTED_QUANTITY);
        assertThat(sixthdayStores.size(), is(0));
    }
    private void assertSkuAvailability(SkuAvailability actual, String status, String addToCartMsg, boolean inventoryAvailable) {
    	assertThat(actual.getStatus(), equalTo(status));
    	assertThat(actual.isInventoryAvailable(), equalTo(inventoryAvailable));
    	assertThat(actual.getAddToCartMessage(), equalTo(addToCartMsg));
    }
    
    @Test(expected = LimitExceededException.class)
    public void shouldThrowExceptionWhenDynamoStoreInventoryRepositoryThrowsLimitExceededException() throws Exception {
    	when(gotWWWRepository.getStores(anyString(), any(), any())).thenReturn(Arrays.asList("1"));
    	when(dynamoStoreInventoryRepository.getAllStoresSkus(any(), anyString())).thenThrow(new LimitExceededException("Expected error from test case"));
        
    	storeSearchService.getStores(BRAND_CODE, storeSearchLocation, Optional.of(RADIUS), SKU_ID, REQUESTED_QUANTITY);
    }
    
    @Test(expected = InternalServerErrorException.class)
    public void shouldThrowExceptionWhenDynamoStoreInventoryRepositoryThrowsInternalServerErrorException() throws Exception {
    	when(gotWWWRepository.getStores(anyString(), any(), any())).thenReturn(Arrays.asList("1"));
    	when(dynamoStoreInventoryRepository.getAllStoresSkus(any(), anyString())).thenThrow(new InternalServerErrorException("Expected error from test case"));
        
    	storeSearchService.getStores(BRAND_CODE, storeSearchLocation, Optional.of(RADIUS), SKU_ID, REQUESTED_QUANTITY);
    }
    
    @Test(expected = Exception.class)
    public void shouldThrowExceptionWhenDynamoStoreInventoryRepositoryThrowsAnyError() throws Exception {
    	when(gotWWWRepository.getStores(anyString(), any(), any())).thenReturn(Arrays.asList("1"));
    	when(dynamoStoreInventoryRepository.getAllStoresSkus(any(), anyString())).thenThrow(new Exception("Expected error from test case"));
        
    	storeSearchService.getStores(BRAND_CODE, storeSearchLocation, Optional.of(RADIUS), SKU_ID, REQUESTED_QUANTITY);
    }
    
}

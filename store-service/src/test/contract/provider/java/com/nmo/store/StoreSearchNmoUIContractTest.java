package com.sixthday.store;

import static com.sixthday.store.config.Constants.Filters.IS_ELIGIBLE_FOR_BOPS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.*;

import com.sixthday.store.data.*;
import com.sixthday.store.models.*;
import com.sixthday.store.repository.dynamodb.StoreInventoryBySKURepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.*;

import com.sixthday.store.controllers.StoreSearchController;
import com.sixthday.store.models.storeindex.StoreDocument;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryDocument;
import com.sixthday.store.repository.ElasticsearchRepository;
import com.sixthday.store.repository.GotWWWRepository;
import com.sixthday.store.repository.dynamodb.DynamoDBStoreInventoryRepository;
import com.sixthday.store.services.StoreSearchService;
import com.sixthday.store.toggles.Features;
import com.sixthday.store.util.sixthdayMDCAdapter;
import com.toggler.core.utils.FeatureToggleRepository;

import au.com.dius.pact.provider.junit.Consumer;
import au.com.dius.pact.provider.junit.PactRunner;
import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.loader.PactBrokerAuth;
import au.com.dius.pact.provider.junit.target.TestTarget;
import au.com.dius.pact.provider.spring.target.MockMvcTarget;

@RunWith(PactRunner.class)
@Provider("store-service")
@Consumer("sixthday-ui")
@PactBroker(protocol = "${pactbroker.protocol}",
        host = "${pactbroker.hostname}",
        port = "${pactbroker.port}",
        authentication = @PactBrokerAuth(username = "${pactbroker.username}", password = "${pactbroker.password}"))
public class StoreSearchsixthdayUIContractTest {

    @InjectMocks
    private StoreSearchService storeSearchService;

    @Mock
    private sixthdayMDCAdapter sixthdayMDCAdapterMock;

    @Mock
    private ElasticsearchRepository elasticsearchRepository;

    @Mock
    private DynamoDBStoreInventoryRepository dynamoDBStoreInventoryRepository;

    @Mock
    private GotWWWRepository gotWWWRepository;

    @InjectMocks
    private StoreSearchController storeSearchController;

    @TestTarget
    public MockMvcTarget target = new MockMvcTarget();

    @Rule
    public FeatureToggleRepository featureToggleRepository = new FeatureToggleRepository();
    
    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        storeSearchController = new StoreSearchController(storeSearchService, sixthdayMDCAdapterMock);
        target.setControllers(storeSearchController);
    }
    
    @After
    public void resetToggle() {
    	featureToggleRepository.disable(Features.READ_SKU_STORES);
    }

    @State("StoreSkuInventoryIsAvailableForAGivenListOfSKUsAndStore")
    public void StoreSkuInventoryIsAvailableForAGivenListOfSKUsAndStore() {
        StoreInventoryBySKUDocumentBuilder storeInventoryBySKUDocumentBuilder = new StoreInventoryBySKUDocumentBuilder();
        StoreInventoryItemBuilder storeInventoryItemBuilder = new StoreInventoryItemBuilder();

        List<StoreInventoryItem> storeInventoryItems1 = Arrays.asList(
                storeInventoryItemBuilder.withAll("Store1", "Store1/1", "Location1", "InventoryLevel1", 10, 10).build(),
                storeInventoryItemBuilder.withAll("Store2", "Store2/2", "Location2", "InventoryLevel2", 20, 20).build()
        );
        StoreInventoryBySKUDocument storeInventoryBySKUDocument1 = storeInventoryBySKUDocumentBuilder.withSkuId("sku1").withStoreInventoryItems(storeInventoryItems1).build();

        List<StoreInventoryItem> storeInventoryItems2 = Arrays.asList(
                storeInventoryItemBuilder.withAll("Store1", "Store1/1", "Location1", "InventoryLevel1", 10, 10).build(),
                storeInventoryItemBuilder.withAll("Store3", "Store3/3", "Location3", "InventoryLevel3", 30, 30).build()
        );
        StoreInventoryBySKUDocument storeInventoryBySKUDocument2 = storeInventoryBySKUDocumentBuilder.withSkuId("sku2").withStoreInventoryItems(storeInventoryItems2).build();

        List<StoreInventoryItem> storeInventoryItems3 = Arrays.asList(
                storeInventoryItemBuilder.withAll("Store2", "Store2/2", "Location2", "InventoryLevel2", 20, 20).build(),
                storeInventoryItemBuilder.withAll("Store3", "Store3/3", "Location3", "InventoryLevel3", 30, 30).build()
        );
        StoreInventoryBySKUDocument storeInventoryBySKUDocument3 = storeInventoryBySKUDocumentBuilder.withSkuId("sku3").withStoreInventoryItems(storeInventoryItems3).build();

        List<StoreInventoryBySKUDocument> storeInventoryBySKUDocuments = Arrays.asList(storeInventoryBySKUDocument1, storeInventoryBySKUDocument2, storeInventoryBySKUDocument3);

        when(dynamoDBStoreInventoryRepository.getSKUsInventory(any(HashSet.class))).thenReturn(storeInventoryBySKUDocuments);
    }

    @State("StoreSkuInventoryIsNotAvailableForAGivenListOfSKUsAndOrStore")
    public void StoreSkuInventoryIsNotAvailableForAGivenListOfSKUsAndStore() {
        when(dynamoDBStoreInventoryRepository.getSKUsInventory(any(HashSet.class))).thenReturn(new ArrayList());
    }

    @State("StoreSkuInventoryIsNotAvailableUnexpectedErrors")
    public void StoreSkuInventoryIsNotAvailableUnexpectedErrors() {
        when(dynamoDBStoreInventoryRepository.getSKUsInventory(any(HashSet.class))).thenThrow(Exception.class);
    }

    @State("StoresNotPresent")
    public void toStoresNotPresentForTheGivenMileRadiusState() {
        when(gotWWWRepository.getStores(eq("nm"), any(StoreSearchLocation.class), any(Optional.class))).thenReturn(Arrays.asList());
        when(elasticsearchRepository.getAllStoresSkus(eq(Arrays.asList()), eq("sku118570150"))).thenReturn(Arrays.asList());
    }
    
    @State("StoresNotPresentWhenReadSkuStoreToggleIsOn")
    public void toStoresNotPresentForTheGivenMileRadiusStateWhenReadSkuStoreToggleIsOn() {
    	featureToggleRepository.enable(Features.READ_SKU_STORES);
        when(gotWWWRepository.getStores(eq("nm"), any(StoreSearchLocation.class), any(Optional.class))).thenReturn(Arrays.asList());
    }   

    @State("StoresPresent")
    public void toStoresPresentForTheDefaultMileRadiusState() {
        List<StoreSkuInventoryDocument> storeSkuInventoryDocuments = Arrays.asList(
                new StoreSkuInventoryDocumentBuilder().withStoreNumber("02").build(),
                new StoreSkuInventoryDocumentBuilder().withStoreNumber("03").build()
        );

        StoreDocument storeDocumentWithEvents = new StoreDocumentBuilder().withStoreNumber("02").withStoreName("Dallas - NorthPark").withId("02/NP")
                .withAddressLine1("8687 North Central Expressway").withAddressLine2("Suite 400").withPhoneNumber("214-363-8311")
                .withStoreHours("Mon. 10:00AM - 9:00PM,Tue. 10:00AM - 9:00PM,Wed. 10:00AM - 9:00PM,Thu. 10:00AM - 9:00PM,Fri. 10:00AM - 9:00PM,Sat. 10:00AM - 8:00PM,Sun. 12:00PM - 6:00PM")
                .withStoreEventDocument().withEventName("abc").withEventDescription("abc").withEventDuration("4 jun")
                .done()
                .build();
        StoreDocument storeDocumentWithoutEvents = new StoreDocumentBuilder().withStoreNumber("03").withStoreName("Fort Worth").withId("03/FW")
                .withAddressLine1("2100 Green Oaks Road ").withAddressLine2(null).withPhoneNumber("817-738-3581")
                .withStoreHours("Mon. 10:00AM - 7:00PM,Tue. 10:00AM - 7:00PM,Wed. 10:00AM - 7:00PM,Thu. 10:00AM - 7:00PM,Fri. 10:00AM - 7:00PM,Sat. 10:00AM - 7:00PM,Sun. 12:00PM - 5:00PM")
                .build();

        List<String> gotWWWStores = Arrays.asList("02", "03");
        List<String> filteredStoreNumbers = Arrays.asList("02", "03");
        List<StoreDocument> storeDocuments = Arrays.asList(storeDocumentWithEvents, storeDocumentWithoutEvents);

        when(gotWWWRepository.getStores(eq("nm"), any(StoreSearchLocation.class), any(Optional.class))).thenReturn(gotWWWStores);
        when(elasticsearchRepository.getAllStoresSkus(eq(gotWWWStores), eq("sku118570150"))).thenReturn(storeSkuInventoryDocuments);
        when(elasticsearchRepository.filterStoresForStoreNumbers(filteredStoreNumbers,IS_ELIGIBLE_FOR_BOPS)).thenReturn(storeDocuments);
    }

    @State("StoresPresentWhenReadSkuStoreToggleIsOn")
    public void toStoresPresentForTheDefaultMileRadiusStateWhenReadSkuStoreToggleIsOn() {
    	featureToggleRepository.enable(Features.READ_SKU_STORES);
        List<StoreSkuInventoryDocument> storeSkuInventoryDocuments = Arrays.asList(
                new StoreSkuInventoryDocumentBuilder().withStoreNumber("02").build(),
                new StoreSkuInventoryDocumentBuilder().withStoreNumber("03").build()
        );

        StoreDocument storeDocumentWithEvents = new StoreDocumentBuilder().withStoreNumber("02").withStoreName("Dallas - NorthPark").withId("02/NP")
                .withAddressLine1("8687 North Central Expressway").withAddressLine2("Suite 400").withPhoneNumber("214-363-8311")
                .withStoreHours("Mon. 10:00AM - 9:00PM,Tue. 10:00AM - 9:00PM,Wed. 10:00AM - 9:00PM,Thu. 10:00AM - 9:00PM,Fri. 10:00AM - 9:00PM,Sat. 10:00AM - 8:00PM,Sun. 12:00PM - 6:00PM")
                .withStoreEventDocument().withEventName("abc").withEventDescription("abc").withEventDuration("4 jun")
                .done()
                .build();
        StoreDocument storeDocumentWithoutEvents = new StoreDocumentBuilder().withStoreNumber("03").withStoreName("Fort Worth").withId("03/FW")
                .withAddressLine1("2100 Green Oaks Road ").withAddressLine2(null).withPhoneNumber("817-738-3581")
                .withStoreHours("Mon. 10:00AM - 7:00PM,Tue. 10:00AM - 7:00PM,Wed. 10:00AM - 7:00PM,Thu. 10:00AM - 7:00PM,Fri. 10:00AM - 7:00PM,Sat. 10:00AM - 7:00PM,Sun. 12:00PM - 5:00PM")
                .build();

        List<String> gotWWWStores = Arrays.asList("02", "03");
        List<String> filteredStoreNumbers = Arrays.asList("02", "03");
        List<StoreDocument> storeDocuments = Arrays.asList(storeDocumentWithEvents, storeDocumentWithoutEvents);

        when(gotWWWRepository.getStores(eq("nm"), any(StoreSearchLocation.class), any(Optional.class))).thenReturn(gotWWWStores);
        when(dynamoDBStoreInventoryRepository.getAllStoresSkus(eq(gotWWWStores), eq("sku118570150"))).thenReturn(storeSkuInventoryDocuments);
        when(elasticsearchRepository.filterStoresForStoreNumbers(filteredStoreNumbers,IS_ELIGIBLE_FOR_BOPS)).thenReturn(storeDocuments);
    }
    
    @State("unWantedRequestParamsForFreeFromAddressWhenReadSkuStoreToggleIsOn")
    public void noStoresPresentForUnWantedRequestParamsForFreeFromAddress() {
    }
   
    @State("unWantedRequestParamsForFreeFromAddressWhenReadSkuStoreToggleIsOn")
    public void noStoresPresentForUnWantedRequestParamsForFreeFromAddressWhenReadSkuStoreToggleIsOn() {
    	featureToggleRepository.enable(Features.READ_SKU_STORES);
    }
}

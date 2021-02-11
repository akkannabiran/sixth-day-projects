package com.sixthday.store;

import static com.jayway.restassured.RestAssured.when;
import static com.sixthday.store.models.SkuAvailabilityInfo.AVAILABLE;
import static com.sixthday.store.models.SkuAvailabilityInfo.AVAILABLE_FOR_PICKUP_TODAY;
import static com.sixthday.store.models.SkuAvailabilityInfo.LIMITED_AVAILABILITY;
import static com.sixthday.store.models.SkuAvailabilityInfo.LIMITED_STOCK;
import static com.sixthday.store.models.SkuAvailabilityInfo.NOT_AVAILABLE;
import static com.sixthday.store.models.SkuAvailabilityInfo.NOT_AVAILABLE_TODAY;
import static com.sixthday.store.models.SkuAvailabilityInfo.PICK_UP_IN_TWO_THREE_DAYS;
import static com.sixthday.store.models.SkuAvailabilityInfo.PICK_UP_TODAY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.elasticsearch.client.transport.TransportClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.ValidatableResponse;
import com.sixthday.store.config.AwsDynamoDbConfig;
import com.sixthday.store.config.ConsulClientConfiguration;
import com.sixthday.store.config.StoreDetailsConfig;
import com.sixthday.store.config.SubscriberConfiguration;
import com.sixthday.store.config.SubscriberConfiguration.QueueConfig;
import com.sixthday.store.config.VaultSecrets;
import com.sixthday.store.data.StoreDocumentBuilder;
import com.sixthday.store.data.StoreInventoryBySKUDocumentBuilder;
import com.sixthday.store.data.StoreInventoryItemBuilder;
import com.sixthday.store.data.StoreResponseForAPIValidation;
import com.sixthday.store.listeners.ApplicationPreparedEventListener;
import com.sixthday.store.listeners.ConsulClientListener;
import com.sixthday.store.models.StoreInventoryBySKUDocument;
import com.sixthday.store.models.StoreInventoryItem;
import com.sixthday.store.models.StoreSearchLocation;
import com.sixthday.store.models.storeindex.StoreDocument;
import com.sixthday.store.repository.ElasticsearchRepository;
import com.sixthday.store.repository.GotWWWRepository;
import com.sixthday.store.repository.dynamodb.StoreInventoryBySKURepository;
import com.sixthday.store.toggles.Features;
import com.toggler.core.utils.FeatureToggleRepository;

import lombok.SneakyThrows;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
properties = {"spring.cloud.consul.enabled=false",
              "spring.cache.type=none",
              "sixthday-store-api.elastic-search-config.store-index-name=StoreIndex",
              "sixthday-store-sub.queue-config.queue-name=StoreQueue",
              "sixthday-store-sub.products-foundation-mode=false",
              "sixthday-store-api.elastic-search-config.store-sku-inventory-index-name=StoreInventoryIndexName",
              "sixthday-store-sub.queue-config.store-sku-inventory-queue-name=StoreInventoryQueue",
              "sixthday-store-sub.store-inventory-by-sku-queue-config.queue-name=StoreInventoryBySKUQueueName",
              "sixthday-dynamodb-config.region=us-west-2", "sixthday-dynamodb-config.table-name-prefix=test_",
              "sixthday-dynamodb-config.endpoint=http://localhost:8000", "sixthday-dynamodb-config.access-key=DummyAccessKey", "sixthday-dynamodb-config.secret-key=DummyKey"})
public class GetStoresWithDynamoDBServiceTest {

    private static final String STORES_SEARCH_URL_WITH_NO_ADDRESS_OR_LAT_LONG = "/stores";
    private static final String STORES_SEARCH_URL_WITH_RADIUS = "/stores?brandCode={brandCode}&freeFormAddress={address}&mileRadius={radius}&skuId={skuId}&quantity={quantity}";

    private static final String BRAND_CODE = "NM";
    private static final String ADDRESS = "ADDRESS";
    private static final String SKU_ID = "skuId";
    private static final int REQUESTED_QUANTITY = 1;

    @Value("${local.server.port}")
    private int port;

    @MockBean
    private StoreDetailsConfig storeDetailsConfig;
    
    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private SubscriberConfiguration subscriberConfiguration;
    
    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private QueueConfig queueConfig;

    @MockBean
    private VaultSecrets vaultSecrets;

    @MockBean
    private ElasticsearchRepository elasticsearchRepository;

    @MockBean
    private StoreInventoryBySKURepository dynamoDBRepository;

    @MockBean
    private GotWWWRepository gotWWWRepository;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private TransportClient transportClient;

    @MockBean(name="listenerContainer", answer = Answers.RETURNS_DEEP_STUBS)
    private SimpleMessageListenerContainer listenerContainer;
    
    @MockBean(name="listenerContainerForStoreSkuInvMessage", answer = Answers.RETURNS_DEEP_STUBS)
    private SimpleMessageListenerContainer listenerContainerForStoreSkuInvMessage;

    @MockBean(name="storeInvBySKUReceiver", answer=Answers.RETURNS_DEEP_STUBS)
    private SimpleMessageListenerContainer listenerContainerForStoreInvBySKUMessage;
    
    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private AmqpAdmin amqpAdmin;

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private ConsulClientConfiguration consulClientConfiguration;

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private ConsulClientListener consulClientListener;

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private AwsDynamoDbConfig awsDynamoDbConfig;

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private ApplicationPreparedEventListener applicationPreparedEventListener;

    @MockBean(name="storeDataQueue", answer = Answers.RETURNS_DEEP_STUBS)
    private Queue storeDataQueue;

    @MockBean(name="storeSkuInventoryQueue", answer = Answers.RETURNS_DEEP_STUBS)
    private Queue storeSkuInventoryQueue;
    
    @MockBean(name="storeInventoryBySkuDetailQueue", answer = Answers.RETURNS_DEEP_STUBS)
    private Queue storeInventoryBySkuDetailQueue;
    
    @Rule
    public FeatureToggleRepository featureToggleRepository = new FeatureToggleRepository();

    @Before
    public void setUp() throws IOException {
        RestAssured.port = port;
        featureToggleRepository.enable(Features.STUB_GOT_WWW);
        featureToggleRepository.enable(Features.READ_SKU_STORES);
    }

    @Test
    public void shouldReturnBadRequestWhenBothAddressAndLatLongAreNotProvided() {
        ValidatableResponse response = when().get(STORES_SEARCH_URL_WITH_NO_ADDRESS_OR_LAT_LONG, new Object[]{}).then();

        response.statusCode(400);
    }

    @Test
    public void shouldFindStoresWithinTheRadiusProvided() {

        ValidatableResponse response = when().get(STORES_SEARCH_URL_WITH_RADIUS, new Object[]{BRAND_CODE, ADDRESS, "12", SKU_ID, REQUESTED_QUANTITY}).then();

        response.statusCode(200);
        verify(gotWWWRepository).getStores(eq(BRAND_CODE), any(StoreSearchLocation.class), eq(Optional.of(12)));
    }
    
    @Test
    @SneakyThrows
    public void shouldRetrunStoresWithAvailableInventoryStatusWhenDynamoDBHasSkuWithAvailableInventory() {
    	StoreInventoryBySKUDocumentBuilder dynamoDocBuilder = new StoreInventoryBySKUDocumentBuilder();
    	StoreInventoryItemBuilder itemBuilder = new StoreInventoryItemBuilder();
        List<StoreInventoryItem> expectedStoreInventories = Arrays.asList(
        		itemBuilder.withAll("01", "01/L1", "001", AVAILABLE, 100, 98).build(),
        		itemBuilder.withAll("02", "02/L2", "002", LIMITED_AVAILABILITY, 5, 5).build(),
        		itemBuilder.withAll("03", "03/L3", "003", NOT_AVAILABLE, 0, 0).build()
        		
        		);
        StoreInventoryBySKUDocument dynamoDoc = dynamoDocBuilder.withSkuId(SKU_ID).withStoreInventoryItems(expectedStoreInventories).build();
        
        StoreDocumentBuilder storeBuilder = new StoreDocumentBuilder();
        List<StoreDocument> storeDocList = Arrays.asList(
        			storeBuilder.withId("01/L1").withStoreNumber("01").build(),
        			storeBuilder.withId("02/L2").withStoreNumber("02").build(),
        			storeBuilder.withId("03/L3").withStoreNumber("03").build()
        		);
        when(gotWWWRepository.getStores(eq(BRAND_CODE), any(StoreSearchLocation.class), eq(Optional.of(12)))).thenReturn(Arrays.asList("01", "02", "03", "098"));
        when(elasticsearchRepository.filterStoresForStoreNumbers(any(), any())).thenReturn(storeDocList);
        when(dynamoDBRepository.findOne(anyString())).thenReturn(dynamoDoc);
        
        ValidatableResponse response = when().get(STORES_SEARCH_URL_WITH_RADIUS, new Object[]{BRAND_CODE, ADDRESS, "12", SKU_ID, REQUESTED_QUANTITY}).then();
        
        response.statusCode(200);
        List<StoreResponseForAPIValidation> stores  = Arrays.asList( new ObjectMapper().readValue(response.extract().asString(), StoreResponseForAPIValidation[].class));
        assertThat("Response should contain 3 stores", stores.size(), equalTo(3));
        validatesixthdayStoreAttributes(stores.get(0), "01", AVAILABLE_FOR_PICKUP_TODAY, true, PICK_UP_TODAY);
        validatesixthdayStoreAttributes(stores.get(1), "02", LIMITED_STOCK, true, PICK_UP_TODAY);
        validatesixthdayStoreAttributes(stores.get(2), "03", NOT_AVAILABLE_TODAY, false, PICK_UP_IN_TWO_THREE_DAYS);
        
    }
   
    private void validatesixthdayStoreAttributes(StoreResponseForAPIValidation actual, String storeNumber, String inventoryStatus, boolean inventoryAvailable, String cartMsg) {
    	assertThat("Store number should match with expected store number", actual.getStoreNumber(), equalTo(storeNumber));
    	assertThat("Inventory stuatus should match with expected inventory status", actual.getSkuAvailability().getStatus(), equalTo(inventoryStatus));
    	assertThat("Inventory availability should match with expected inventory availability", actual.getSkuAvailability().isInventoryAvailable(), equalTo(inventoryAvailable));
    	assertThat("Add to cart message should match with expected message", actual.getSkuAvailability().getAddToCartMessage(), equalTo(cartMsg));
    	
    	assertThat("StoreId should not be null", actual.getStoreId(), notNullValue());
    }
}

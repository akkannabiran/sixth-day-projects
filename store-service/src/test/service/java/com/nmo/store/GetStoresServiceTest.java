package com.sixthday.store;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.ValidatableResponse;
import com.sixthday.store.config.*;
import com.sixthday.store.config.SubscriberConfiguration.QueueConfig;
import com.sixthday.store.data.StoreInventoryBySKUDocumentBuilder;
import com.sixthday.store.data.StoreInventoryItemBuilder;
import com.sixthday.store.listeners.ApplicationPreparedEventListener;
import com.sixthday.store.listeners.ConsulClientListener;
import com.sixthday.store.models.*;
import com.sixthday.store.repository.ElasticsearchRepository;
import com.sixthday.store.repository.GotWWWRepository;
import com.sixthday.store.repository.dynamodb.StoreInventoryBySKURepository;
import com.sixthday.store.toggles.Features;
import com.toggler.core.utils.FeatureToggleRepository;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
properties = {"spring.cloud.consul.enabled=false",
              "spring.cache.type=none",
              "sixthday-store-api.elastic-search-config.store-index-name=StoreIndex",
              "sixthday-store-sub.products-foundation-mode=false",
              "sixthday-store-sub.queue-config.queue-name=StoreQueue",
              "sixthday-store-api.elastic-search-config.store-sku-inventory-index-name=StoreInventoryIndexName",
              "sixthday-store-sub.queue-config.store-sku-inventory-queue-name=StoreInventoryQueue",
              "sixthday-store-sub.store-inventory-by-sku-queue-config.queue-name=StoreInventoryBySKUQueueName",
              "sixthday-dynamodb-config.region=us-west-2", "sixthday-dynamodb-config.table-name-prefix=test_",
              "sixthday-dynamodb-config.endpoint=http://localhost:8000", "sixthday-dynamodb-config.access-key=DummyAccessKey", "sixthday-dynamodb-config.secret-key=DummyKey"})
public class GetStoresServiceTest {

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
    private StoreInventoryBySKURepository storeInventoryBySKURepository;

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
    }

    @Test
    public void shouldReturnInternalErrorWhenSetSKUsInventoryCalled() {
        Mockito.when(storeInventoryBySKURepository.findAll(any(HashSet.class))).thenThrow(Exception.class);
        ValidatableResponse response = when().get("/skuInventoryByStore?storeId={storeId}&skuIds={skuIds}&caller={caller}",
                new Object[]{"01", "sku103460468", "sixthday-UI"}).then();
        response.statusCode(500);
    }

    @Test
    public void shouldReturnStoreSkuInventoryWhenSetSKUsInventoryCalled() {
        StoreInventoryBySKUDocumentBuilder storeInventoryBySKUDocumentBuilder = new StoreInventoryBySKUDocumentBuilder();
        StoreInventoryItemBuilder storeInventoryItemBuilder = new StoreInventoryItemBuilder();

        List<StoreInventoryItem> storeInventoryItems1 = Arrays.asList(
                storeInventoryItemBuilder.withAll("s11", "s1/1", "l1", "l1", 1, 1).build(),
                storeInventoryItemBuilder.withAll("s21", "s2/2", "l2", "l2", 2, 2).build(),
                storeInventoryItemBuilder.withAll("s31", "s3/3", "l3", "l3", 3, 3).build()
        );
        StoreInventoryBySKUDocument storeInventoryBySKUDocument1 = storeInventoryBySKUDocumentBuilder.withSkuId("sku1").withStoreInventoryItems(storeInventoryItems1).build();

        List<StoreInventoryItem> storeInventoryItems2 = Arrays.asList(
                storeInventoryItemBuilder.withAll("s21", "s2/2", "l2", "l2", 2, 2).build(),
                storeInventoryItemBuilder.withAll("s31", "s3/3", "l3", "l3", 3, 3).build()
        );
        StoreInventoryBySKUDocument storeInventoryBySKUDocument2 = storeInventoryBySKUDocumentBuilder.withSkuId("sku2").withStoreInventoryItems(storeInventoryItems2).build();

        List<StoreInventoryItem> storeInventoryItems3 = Arrays.asList(
                storeInventoryItemBuilder.withAll("s11", "s1/1", "l1", "l1", 1, 1).build(),
                storeInventoryItemBuilder.withAll("s22", "s2/2", "l2", "l2", 2, 2).build(),
                storeInventoryItemBuilder.withAll("s33", "s3/3", "l3", "l3", 3, 3).build()
        );
        StoreInventoryBySKUDocument storeInventoryBySKUDocument3 = storeInventoryBySKUDocumentBuilder.withSkuId("sku3").withStoreInventoryItems(storeInventoryItems3).build();

        List<StoreInventoryBySKUDocument> storeInventoryBySKUDocuments = Arrays.asList(storeInventoryBySKUDocument1, storeInventoryBySKUDocument2, storeInventoryBySKUDocument3);


        Mockito.when(storeInventoryBySKURepository.findAll(any(HashSet.class))).thenReturn(storeInventoryBySKUDocuments);
        ValidatableResponse response = when().get("/skuInventoryByStore?storeId={storeId}&skuIds={skuIds}&caller={caller}", new Object[]{"s11", "sku1,sku2,sku3", "sixthday-UI"}).then();
        response
                .statusCode(200)
                .body("store.storeNumber", is("s11"))
                .body("store.storeId", is("s1/1"))
                .body("store.locationNumber", is("l1"))
                .body("skuInventories[0].skuId", is("sku1"))
                .body("skuInventories[0].inventoryLevel", is("l1"))
                .body("skuInventories[0].quantity", is(1))
                .body("skuInventories[0].bopsQuantity", is(1))
                .body("skuInventories[1].skuId", is("sku3"))
                .body("skuInventories[1].inventoryLevel", is("l1"))
                .body("skuInventories[1].quantity", is(1))
                .body("skuInventories[1].bopsQuantity", is(1));
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
}

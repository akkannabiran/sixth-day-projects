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
import static com.sixthday.store.models.storeinventoryindex.SkuStoresInventoryMessage.EventType.SKU_STORES_INVENTORY_UPDATED;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.ValidatableResponse;
import com.sixthday.store.data.SkuStoresInventoryMessageBuilder;
import com.sixthday.store.data.StoreMessageBuilder;
import com.sixthday.store.data.StoreResponseForAPIValidation;
import com.sixthday.store.exceptions.GotWWWCommunicationException;
import com.sixthday.store.models.storeinventoryindex.SkuStore;
import com.sixthday.store.models.storeinventoryindex.StoreInventory;
import com.sixthday.store.repository.GotWWWRepository;
import com.sixthday.store.toggles.Features;
import com.toggler.core.utils.FeatureToggleRepository;

import lombok.SneakyThrows;
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {StoreLocatorServiceApplication.class},
			webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
            properties = {"spring.cache.type=none", "sixthday-store-sub.products-foundation-mode=false","spring.cloud.stream.bindings.storeInventoryBySKU.binder=test"})
public class SkuStoresInventoryIntegrationTest {
  
  private static final long PROPAGATION_WAIT_TIME = 100;
  private static final int REQUESTED_QTY = 100;
  private static final String STORES_SEARCH_URL_WITH_RADIUS = "/stores?brandCode={brandCode}&freeFormAddress={address}&mileRadius={radius}&skuId={skuId}&quantity={quantity}";

  private static final String BRAND_CODE = "NM";
  private static final String ADDRESS = "ADDRESS";
  @Value("${local.server.port}")
  private int port;
  
  @Autowired
  private RabbitTemplate rabbitTemplate;
  
  @Autowired
  private Queue storeInventoryBySkuDetailQueue;
  
  @Autowired
  private Queue storeDetailQueue;
  
  @MockBean
  private GotWWWRepository gotWWWRepository;
  
  @Rule
  public FeatureToggleRepository featureToggleRepository = new FeatureToggleRepository();
  
  @Before
  public void setUp() {
	RestAssured.port = port;
    featureToggleRepository.enable(Features.STUB_GOT_WWW);
    featureToggleRepository.enable(Features.READ_SKU_STORES);
  }
  
  @SneakyThrows
  private void sendMessageToRabbitMQ(String payload, String queueName) {
    Message message = MessageBuilder.withBody(payload.getBytes()).setContentType(MessageProperties.CONTENT_TYPE_JSON).build();
    rabbitTemplate.convertAndSend(queueName, message);
  }

  @SneakyThrows
  private void loadIntegrationTestStores() {
	  StoreMessageBuilder builder = new StoreMessageBuilder().withEligibleForBOPS(true).withDisplayable(true);
	  sendMessageToRabbitMQ(builder.withId("99901L1").withStoreNumber("99901").buildAsJsonString(), storeDetailQueue.getName());
	  sendMessageToRabbitMQ(builder.withId("99902L2").withStoreNumber("99902").buildAsJsonString(), storeDetailQueue.getName());
	  sendMessageToRabbitMQ(builder.withId("99903L3").withStoreNumber("99903").buildAsJsonString(), storeDetailQueue.getName());
	  sendMessageToRabbitMQ(builder.withId("99913L4").withStoreNumber("99913").buildAsJsonString(), storeDetailQueue.getName());
	  TimeUnit.SECONDS.sleep(2);
  }
  
  private StoreInventory buildStoreInventory(String storeNumber, String locationNumber, String invLevel, int quantity, int bopsQuantity) {
    StoreInventory inventory =  new StoreInventory();
    inventory.setStoreNumber(storeNumber);
    inventory.setLocationNumber(locationNumber);
    inventory.setStoreId(storeNumber+"/"+locationNumber);
    inventory.setInvLevel(invLevel);
    inventory.setQuantity(quantity);
    inventory.setBopsQuantity(bopsQuantity);
    return inventory;
  }

  /**
   * Generates sku stores inventory message with store numbers 01, 02, 02, 13 and unavailable store number A and publishes the message to RabbitMQ.
   * Each store has available, limited, not available inventories.
   * Each sku has available, limited, not available inventories along with equal to, more than and less than requested qty. 
   */
  private void prepareAndPublishSkuData() {
    SkuStoresInventoryMessageBuilder msg = new SkuStoresInventoryMessageBuilder();
    msg.withBatchId("BatchId").withEventType(SKU_STORES_INVENTORY_UPDATED).withProductId("integration-test-product1");
    
    SkuStore sku1 = new SkuStore("integration-test-sku1", Arrays.asList(
            buildStoreInventory("99901", "L1", AVAILABLE, 100, 100),
            buildStoreInventory("99902", "L2", LIMITED_AVAILABILITY, 200, 200),
            buildStoreInventory("99903", "L3", NOT_AVAILABLE, 10, 10),
            buildStoreInventory("99913", "L4", "X", 1000, 10),
            buildStoreInventory("A", "LA", "X", 0, 10)
            ));
    SkuStore sku2 = new SkuStore("integration-test-sku2", Arrays.asList(
            buildStoreInventory("99902", "L2", AVAILABLE, 100, 100),
            buildStoreInventory("99903", "L3", LIMITED_AVAILABILITY, 200, 200),
            buildStoreInventory("99913", "L4", NOT_AVAILABLE, 10, 10),
            buildStoreInventory("99901", "L1", "X", 1000, 10),
            buildStoreInventory("A", "LA", AVAILABLE, 1000, 10)
            ));
    SkuStore sku3 = new SkuStore("integration-test-sku3", Arrays.asList(
            buildStoreInventory("99903", "L3", LIMITED_AVAILABILITY, 100, 100),
            buildStoreInventory("99913", "L4", LIMITED_AVAILABILITY, 200, 200),
            buildStoreInventory("99901", "L1", LIMITED_AVAILABILITY, 10, 10),
            buildStoreInventory("99902", "L2", LIMITED_AVAILABILITY, 1000, 10),
            buildStoreInventory("A", "LA", LIMITED_AVAILABILITY, 1000, 10)
            ));
    SkuStore sku4 = new SkuStore("integration-test-sku4", Arrays.asList(
            buildStoreInventory("99913", "L4", AVAILABLE, 100, 100),
            buildStoreInventory("99901", "L1", AVAILABLE, 200, 200),
            buildStoreInventory("99902", "L2", AVAILABLE, 10, 10),
            buildStoreInventory("99903", "L3", AVAILABLE, 1000, 10),
            buildStoreInventory("A", "LA", AVAILABLE, 1000, 10)
            ));
    msg.withSkuStores(Arrays.asList(sku1, sku2, sku3, sku4));
    sendMessageToRabbitMQ(msg.toJson(), storeInventoryBySkuDetailQueue.getName());
  }
  
  @Test
  @SneakyThrows
  public void shouldProvideLatestStoreResponseWhenSkuStoresInventoryMessageIsProcessed() {
	loadIntegrationTestStores();
	prepareAndPublishSkuData();
    
    TimeUnit.MILLISECONDS.sleep(PROPAGATION_WAIT_TIME);
    
    when(gotWWWRepository.getStores(anyString(), any(), any())).thenReturn(Arrays.asList("99901", "99902", "99903", "99913"));

    ValidatableResponse response = when().get(STORES_SEARCH_URL_WITH_RADIUS, new Object[]{BRAND_CODE, ADDRESS, "50", "integration-test-sku1", REQUESTED_QTY}).then();
    response.statusCode(200);
    ObjectMapper objectMapper = new ObjectMapper();
	List<StoreResponseForAPIValidation> stores  = Arrays.asList(objectMapper.readValue(response.extract().asString(), StoreResponseForAPIValidation[].class));
    assertThat("Response should contain 4 stores along with inventory availability", stores.size(), equalTo(4));
    validatesixthdayStoreAttributes(stores.get(0), "99901", AVAILABLE_FOR_PICKUP_TODAY, true, PICK_UP_TODAY);
    validatesixthdayStoreAttributes(stores.get(1), "99902", LIMITED_STOCK, true, PICK_UP_TODAY);
    validatesixthdayStoreAttributes(stores.get(2), "99903", NOT_AVAILABLE_TODAY, false, PICK_UP_IN_TWO_THREE_DAYS);
    validatesixthdayStoreAttributes(stores.get(3), "99913", NOT_AVAILABLE_TODAY, false, PICK_UP_IN_TWO_THREE_DAYS);
    

    response = when().get(STORES_SEARCH_URL_WITH_RADIUS, new Object[]{BRAND_CODE, ADDRESS, "50", "integration-test-sku2", REQUESTED_QTY}).then();
    response.statusCode(200);
    stores  = Arrays.asList(objectMapper.readValue(response.extract().asString(), StoreResponseForAPIValidation[].class));
    assertThat("Response should contain 4 stores along with inventory availability", stores.size(), equalTo(4));
    validatesixthdayStoreAttributes(stores.get(0), "99901", NOT_AVAILABLE_TODAY, false, PICK_UP_IN_TWO_THREE_DAYS);
    validatesixthdayStoreAttributes(stores.get(1), "99902", AVAILABLE_FOR_PICKUP_TODAY, true, PICK_UP_TODAY);
    validatesixthdayStoreAttributes(stores.get(2), "99903", LIMITED_STOCK, true, PICK_UP_TODAY);
    validatesixthdayStoreAttributes(stores.get(3), "99913", NOT_AVAILABLE_TODAY, false, PICK_UP_IN_TWO_THREE_DAYS);
    
    response = when().get(STORES_SEARCH_URL_WITH_RADIUS, new Object[]{BRAND_CODE, ADDRESS, "50", "integration-test-sku3", REQUESTED_QTY}).then();
    response.statusCode(200);
    stores  = Arrays.asList(objectMapper.readValue(response.extract().asString(), StoreResponseForAPIValidation[].class));
    assertThat("Response should contain 4 stores along with inventory availability", stores.size(), equalTo(4));
    validatesixthdayStoreAttributes(stores.get(0), "99901", NOT_AVAILABLE_TODAY, false, PICK_UP_IN_TWO_THREE_DAYS);
    validatesixthdayStoreAttributes(stores.get(1), "99902", LIMITED_STOCK, true, PICK_UP_TODAY);
    validatesixthdayStoreAttributes(stores.get(2), "99903", LIMITED_STOCK, true, PICK_UP_TODAY);
    validatesixthdayStoreAttributes(stores.get(3), "99913", LIMITED_STOCK, true, PICK_UP_TODAY);
    
    response = when().get(STORES_SEARCH_URL_WITH_RADIUS, new Object[]{BRAND_CODE, ADDRESS, "50", "integration-test-sku4", REQUESTED_QTY}).then();
    response.statusCode(200);
    stores  = Arrays.asList(objectMapper.readValue(response.extract().asString(), StoreResponseForAPIValidation[].class));

    assertThat("Response should contain 4 stores along with inventory availability", stores.size(), equalTo(4));
    validatesixthdayStoreAttributes(stores.get(0), "99901", AVAILABLE_FOR_PICKUP_TODAY, true, PICK_UP_TODAY);
    validatesixthdayStoreAttributes(stores.get(1), "99902", NOT_AVAILABLE_TODAY, false, PICK_UP_IN_TWO_THREE_DAYS);
    validatesixthdayStoreAttributes(stores.get(2), "99903", AVAILABLE_FOR_PICKUP_TODAY, true, PICK_UP_TODAY);
    validatesixthdayStoreAttributes(stores.get(3), "99913", AVAILABLE_FOR_PICKUP_TODAY, true, PICK_UP_TODAY);
    
    response = when().get(STORES_SEARCH_URL_WITH_RADIUS, new Object[]{BRAND_CODE, ADDRESS, "50", "sku-that-doesnt-exist", REQUESTED_QTY}).then();
    response.statusCode(200);
    stores  = Arrays.asList(objectMapper.readValue(response.extract().asString(), StoreResponseForAPIValidation[].class));
    validatesixthdayStoreAttributes(stores.get(0), "99901", NOT_AVAILABLE_TODAY, false, PICK_UP_IN_TWO_THREE_DAYS);
    validatesixthdayStoreAttributes(stores.get(1), "99902", NOT_AVAILABLE_TODAY, false, PICK_UP_IN_TWO_THREE_DAYS);
    validatesixthdayStoreAttributes(stores.get(2), "99903", NOT_AVAILABLE_TODAY, false, PICK_UP_IN_TWO_THREE_DAYS);
    validatesixthdayStoreAttributes(stores.get(3), "99913", NOT_AVAILABLE_TODAY, false, PICK_UP_IN_TWO_THREE_DAYS);
    
    assertThat("Response should still contain 4 stores when requested skus inventory data doesn't exist on those stores", stores.size(), equalTo(4));
    
    when(gotWWWRepository.getStores(anyString(), any(), any())).thenReturn(Arrays.asList("This", "is", "negative", "test"));
    response = when().get(STORES_SEARCH_URL_WITH_RADIUS, new Object[]{BRAND_CODE, ADDRESS, "50", "sku-that-doesnt-matter", REQUESTED_QTY}).then();
    response.statusCode(200);
    stores  = Arrays.asList(objectMapper.readValue(response.extract().asString(), StoreResponseForAPIValidation[].class));
    assertThat("Response should not contain any stores when stores from GOTWWW doesn't exist", stores.size(), equalTo(0));
    
    when(gotWWWRepository.getStores(anyString(), any(), any())).thenReturn(Collections.emptyList());
    response = when().get(STORES_SEARCH_URL_WITH_RADIUS, new Object[]{BRAND_CODE, ADDRESS, "50", "sku-that-doesnt-matter", REQUESTED_QTY}).then();
    response.statusCode(200);
    stores  = Arrays.asList(objectMapper.readValue(response.extract().asString(), StoreResponseForAPIValidation[].class));
    assertThat("Response should not contain any stores when no stores found with in raduis of address", stores.size(), equalTo(0));
    
    when(gotWWWRepository.getStores(anyString(), any(), any())).thenThrow(new GotWWWCommunicationException(new Exception("GotLocations timed out.")));
    response = when().get(STORES_SEARCH_URL_WITH_RADIUS, new Object[]{BRAND_CODE, ADDRESS, "50", "sku-that-doesnt-matter", REQUESTED_QTY}).then();
    //When got locations times out, service should throw hystrix timeout
    response.statusCode(424);
    
  }
  
  private void validatesixthdayStoreAttributes(StoreResponseForAPIValidation actual, String storeNumber, String inventoryStatus, boolean inventoryAvailable, String cartMsg) {
    assertThat("Store number should match with expected store number", actual.getStoreNumber(), equalTo(storeNumber));
    assertThat("Inventory stuatus should match with expected inventory status", actual.getSkuAvailability().getStatus(), equalTo(inventoryStatus));
    assertThat("Inventory availability should match with expected inventory availability", actual.getSkuAvailability().isInventoryAvailable(), equalTo(inventoryAvailable));
    assertThat("Add to cart message should match with expected message", actual.getSkuAvailability().getAddToCartMessage(), equalTo(cartMsg));
  }
  
}
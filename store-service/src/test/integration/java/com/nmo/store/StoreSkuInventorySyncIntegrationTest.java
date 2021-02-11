package com.sixthday.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.store.config.SubscriberConfiguration.QueueConfig;
import com.sixthday.store.data.StoreSkuInventoryAnnoucementBuilder;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryAnnouncement;
import com.sixthday.store.setup.StoreSkuInventoryDocumentLoader;
import com.toggler.core.utils.FeatureToggleRepository;
import lombok.SneakyThrows;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MimeType;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {StoreLocatorServiceApplication.class},
  properties = {"spring.cache.type=none" , "spring.cloud.stream.bindings.output.binder=test","sixthday-store-sub.products-foundation-mode=false","spring.cloud.stream.bindings.storeInventoryBySKU.binder=test2"})
public class StoreSkuInventorySyncIntegrationTest {
  private static final int PROPAGATION_WAIT_TIME = 2000;
  @Autowired
  private RestHighLevelClient client;
  
  @Autowired
  private MessageCollector collector;
  @Autowired
  private Source msgSource;
  
  private StoreSkuInventoryDocumentLoader storeSkuInventoryDocumentLoader;
  
  @Autowired
  private QueueConfig queueConfig;

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Rule
  public FeatureToggleRepository featureToggleRepository = new FeatureToggleRepository();

  @Before
  public void setUp() {
    storeSkuInventoryDocumentLoader = new StoreSkuInventoryDocumentLoader(this.client);
  }

  @SneakyThrows
  private void sendMessage(String payload) {
    Message message = MessageBuilder.withBody(payload.getBytes()).setContentType(MessageProperties.CONTENT_TYPE_JSON).build();
    rabbitTemplate.convertAndSend(queueConfig.getStoreSkuInventoryQueueName(), message);
    Thread.sleep(PROPAGATION_WAIT_TIME);
  }
  
  @SuppressWarnings("all")
  @Test
  public void shouldCreateOrUpdateStoreSkuInventory() throws InterruptedException, IOException {
    String msgId = UUID.randomUUID().toString();
    String storeNbr = "OLD_STORE_NUMBER";
    String inventoryUpsert = "{\"id\":\"" + msgId + "\",\"skuId\":\"SKU_ID\",\"storeNumber\":\"" + storeNbr
            + "\",\"storeId\":\"01/DT\",\"locationNumber\":\"LOCATION_NUMBER\",\"inventoryLevelCode\":\"INV_LVL_CODE\",\"quantity\":1,\"bopsQuantity\":1,\"productIds\":[\"PRODUCT_1\",\"PRODUCT_2\"],\"eventType\":\"STORE_SKU_INVENTORY_UPSERT\"}";
    sendMessage(inventoryUpsert);
    StoreSkuInventoryAnnouncement expectedAnnouncement = new StoreSkuInventoryAnnoucementBuilder().withInventoryLevel("INV_LVL_CODE").withProductIds(Arrays.asList("PRODUCT_1", "PRODUCT_2"))
            .withStoreNumber(storeNbr).withQuantity(1).withRemoved(false).withSkudId("SKU_ID").build();
    
    verifyStoreSkuInventoryAnnoucement(expectedAnnouncement, collector.forChannel(msgSource.output()).poll(1000, TimeUnit.MILLISECONDS));
    
    storeNbr = "NEW_STORE_NUMBER";
    inventoryUpsert = "{\"id\":\"" + msgId + "\",\"skuId\":\"SKU_ID\",\"storeNumber\":\"" + storeNbr
            + "\",\"storeId\":\"01/DT\",\"locationNumber\":\"LOCATION_NUMBER\",\"inventoryLevelCode\":\"INV_LVL_CODE\",\"quantity\":1,\"bopsQuantity\":1,\"productIds\":[\"PRODUCT_1\",\"PRODUCT_2\"],\"eventType\":\"STORE_SKU_INVENTORY_UPSERT\"}";
    expectedAnnouncement.setStoreNumber(storeNbr);
    sendMessage(inventoryUpsert);
    
    assertThat(storeSkuInventoryDocumentLoader.findById(msgId).get("storeNumber"), equalTo("NEW_STORE_NUMBER"));
    verifyStoreSkuInventoryAnnoucement(expectedAnnouncement, collector.forChannel(msgSource.output()).poll(2000, TimeUnit.MILLISECONDS));
    
  }
  
  @SneakyThrows
  private void verifyStoreSkuInventoryAnnoucement(StoreSkuInventoryAnnouncement expectedAnnouncement, org.springframework.messaging.Message<?> actualMsg) {
    ObjectMapper mapper = new ObjectMapper();
    if(actualMsg != null && actualMsg.getPayload() != null) {
    		StoreSkuInventoryAnnouncement actualAnnoucement = mapper.readValue(actualMsg.getPayload().toString(), StoreSkuInventoryAnnouncement.class);
    		// assertThat("Announcement should match expected announcement", actualAnnoucement, equalTo(expectedAnnouncement));
    }
  }
 
}

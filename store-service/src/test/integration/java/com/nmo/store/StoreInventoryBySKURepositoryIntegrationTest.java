package com.sixthday.store;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.sixthday.store.models.storeinventoryindex.SkuStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.sixthday.store.data.StoreInventoryBySKUDocumentBuilder;
import com.sixthday.store.data.StoreInventoryItemBuilder;
import com.sixthday.store.data.SkuStoresInventoryMessageBuilder;
import com.sixthday.store.models.StoreInventoryBySKUDocument;
import com.sixthday.store.models.StoreInventoryItem;
import com.sixthday.store.models.storeinventoryindex.StoreInventory;
import static com.sixthday.store.models.storeinventoryindex.SkuStoresInventoryMessage.EventType.SKU_STORES_INVENTORY_UPDATED;
import com.sixthday.store.setup.DynamoDBUtils;

import lombok.SneakyThrows;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {StoreLocatorServiceApplication.class},
                properties = {"spring.cache.type=none" , "sixthday-store-sub.products-foundation-mode=false","spring.cloud.stream.bindings.storeInventoryBySKU.binder=test"})
public class StoreInventoryBySKURepositoryIntegrationTest {
  
  final long PROPAGATION_WAIT_TIME = 100;
  
  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Autowired
  private AmazonDynamoDB amazonDynamoDB;

  @Autowired DynamoDBMapperConfig dynamoDBMapperConfig;

  @Autowired
  private Queue storeInventoryBySkuDetailQueue;
  
  @Before
  public void setUp() {}
  
  @SneakyThrows
  private void sendMessageToStoreSkuInventoryForProductQueue(String payload) {
    Message message = MessageBuilder.withBody(payload.getBytes()).setContentType(MessageProperties.CONTENT_TYPE_JSON).build();
    rabbitTemplate.convertAndSend(storeInventoryBySkuDetailQueue.getName(), message);
    Thread.sleep(PROPAGATION_WAIT_TIME);
  }
  
  @Test
  public void shouldCreateOrUpdateStoreInventoryBySKUDocuments() throws InterruptedException {
    SkuStoresInventoryMessageBuilder msg = new SkuStoresInventoryMessageBuilder();
    msg.withBatchId("BatchId").withEventType(SKU_STORES_INVENTORY_UPDATED).withProductId("PRODUCT-ID");
    List<SkuStore> skusStores = IntStream.range(0, 2).mapToObj(i -> {
      SkuStore sku = new SkuStore();
      sku.setStoreInventories(IntStream.range(0, 2).mapToObj(k -> {
        StoreInventory inventory = new StoreInventory();
        inventory.setStoreId("STR-" + k + "/LCN-" + k);
        inventory.setStoreNumber("STR-" + k);
        inventory.setLocationNumber("LCN-" + k);
        inventory.setInvLevel("01");
        inventory.setBopsQuantity(100 + k + i);
        inventory.setQuantity(200 + k + i);
        return inventory;
      }).collect(Collectors.toList()));
      sku.setSkuId("SKU-" + i);
      return sku;
    }).collect(Collectors.toList());
    
    msg.withSkuStores(skusStores);
    sendMessageToStoreSkuInventoryForProductQueue(msg.toJson());
    
    StoreInventoryBySKUDocumentBuilder builder = new StoreInventoryBySKUDocumentBuilder();
    StoreInventoryItemBuilder itemBuilder = new StoreInventoryItemBuilder();
    StoreInventoryItem storeItem1 = itemBuilder.withStoreNumber("STR-0").withStoreId("STR-0/LCN-0").withLocationNumber("LCN-0").withBopsQuantity(100).withQuanity(200).build();
    StoreInventoryItem storeItem2 = itemBuilder.withStoreNumber("STR-1").withStoreId("STR-1/LCN-1").withLocationNumber("LCN-1").withBopsQuantity(101).withQuanity(201).build();
    StoreInventoryBySKUDocument sku0 = builder.withSkuId("SKU-0").withStoreInventoryItems(Arrays.asList(storeItem1, storeItem2)).build();
    
    StoreInventoryItem storeItem3 = itemBuilder.withStoreNumber("STR-0").withStoreId("STR-0/LCN-0").withLocationNumber("LCN-0").withBopsQuantity(101).withQuanity(201).build();
    StoreInventoryItem storeItem4 = itemBuilder.withStoreNumber("STR-1").withStoreId("STR-1/LCN-1").withLocationNumber("LCN-1").withBopsQuantity(102).withQuanity(202).build();
    
    StoreInventoryBySKUDocument sku1 = builder.withSkuId("SKU-1").withStoreInventoryItems(Arrays.asList(storeItem3, storeItem4)).build();
    
    List<StoreInventoryBySKUDocument> query0Results = DynamoDBUtils.getStoresInventoryBySkuId("SKU-0", dynamoDBMapperConfig, amazonDynamoDB);
    assertThat("Store sku invnetory in dynamo db are same as expected data", query0Results.get(0), equalTo(sku0));
    
    List<StoreInventoryBySKUDocument> query1Results = DynamoDBUtils.getStoresInventoryBySkuId("SKU-1",  dynamoDBMapperConfig, amazonDynamoDB);
    assertThat("Store sku invnetory in dynamo db are same as expected data", query1Results.get(0), equalTo(sku1));
  }
  
}

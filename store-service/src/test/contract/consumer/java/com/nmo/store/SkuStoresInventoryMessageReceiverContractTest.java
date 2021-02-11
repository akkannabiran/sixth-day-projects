package com.sixthday.store;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.store.models.storeinventoryindex.SkuStore;
import com.sixthday.store.models.storeinventoryindex.SkuStoresInventoryMessage;
import com.sixthday.store.models.storeinventoryindex.StoreInventory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.amqp.core.Message;

import com.sixthday.store.cloud.stream.StoreInventoryBySKUAnnouncer;
import com.sixthday.store.handlers.SkuStoresInventoryMessageHandler;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryMessage.EventType;
import static com.sixthday.store.models.storeinventoryindex.SkuStoresInventoryMessage.EventType.*;
import com.sixthday.store.services.StoreSkuInventorySyncService;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.MessagePactProviderRule;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.model.v3.messaging.MessagePact;

@RunWith(MockitoJUnitRunner.class)
public class SkuStoresInventoryMessageReceiverContractTest {

  
  @Mock
  private StoreSkuInventorySyncService service;

  @Mock
  private StoreInventoryBySKUAnnouncer announcer;
  
  @Mock(answer=Answers.RETURNS_DEEP_STUBS)
  private Message message;
  
  @InjectMocks
  private SkuStoresInventoryMessageHandler handler;
  
  @Rule
  public MessagePactProviderRule mockProvider = new MessagePactProviderRule("ATG-rabbitmq", this);

  @Pact(provider = "ATG-rabbitmq", consumer = "store-service")
  public MessagePact createPactForValidSkuStoresInventoryUpdatedMessage(MessagePactBuilder builder) {
      PactDslJsonBody body = new PactDslJsonBody();

      body.stringType("id", "product1234");
      
      body.array("skuStores").object().stringType("skuId", "sku1")
                                          .array("storeInventories")
                                            .object()
                                              .stringType("storeNo", "store1")
                                              .stringType("storeId", "store1/location1")
                                              .stringType("locationNumber", "location1")
                                              .stringType("invLevel", "1")
                                              .integerType("bopsQty", 35)
                                              .integerType("qty", 100).closeObject()
                                              .object()
                                              .stringType("storeNo", "store2")
                                              .stringType("storeId", "store2/location2")
                                              .stringType("locationNumber", "location2")
                                              .stringType("invLevel", "1")
                                              .integerType("bopsQty", 50)
                                              .integerType("qty", 80).closeObject()
                                         .closeArray()
                                  .closeObject()
                                  .object().stringType("skuId", "sku2")
                                    .array("storeInventories")
                                      .object()
                                        .stringType("storeNo", "store1")
                                        .stringType("storeId", "store1/location1")
                                        .stringType("locationNumber", "location1")
                                        .stringType("invLevel", "3")
                                        .integerType("bopsQty", 50)
                                        .integerType("qty", 96).closeObject()
                                      .object()
                                        .stringType("storeNo", "store2")
                                        .stringType("storeId", "store2/location2")
                                        .stringType("locationNumber", "location2")
                                        .stringType("invLevel", "2")
                                        .integerType("bopsQty", 8)
                                        .integerType("qty", 8).closeObject()
                                    .closeArray()
                          .closeObject().closeArray();
      body.stringType("batchId", "SomeGeneratedBatchId").stringValue("eventType", "SKU_STORES_INVENTORY_UPDATED");
      body.object("originTimestampInfo").stringType("SKU_STORES_INVENTORY_UPDATED", "2018-04-19T10:44:34.238").closeObject();
      

      Map<String, String> metadata = new HashMap<>();
      metadata.put("contentType", "application/json");

      return builder.given("store inventory for product message with eventType SKU_STORES_INVENTORY_UPDATED")
              .expectsToReceive("valid store inventory message for product")
              .withMetadata(metadata)
              .withContent(body)
              .toPact();
  }

  @Test
  @PactVerification(value = "ATG-rabbitmq", fragment = "createPactForValidSkuStoresInventoryUpdatedMessage")
  public void responseWhenValidUpsertEvent() throws Exception {
    byte[] currentMessage = mockProvider.getMessage();
    when(message.getBody()).thenReturn(currentMessage);
    new String(currentMessage);
    when(message.getMessageProperties().getType()).thenReturn(SKU_STORES_INVENTORY_UPDATED.name());
    handler.handle(message);
    ArgumentCaptor<SkuStoresInventoryMessage> msgCaptor = ArgumentCaptor.forClass(SkuStoresInventoryMessage.class);
    ArgumentCaptor<SkuStoresInventoryMessage> announcementCaptor = ArgumentCaptor.forClass(SkuStoresInventoryMessage.class);
    verify(service).updateStoreSkuInventory(msgCaptor.capture());
    verify(announcer).announceStoreSkuInventoryForProductMessage(announcementCaptor.capture());
    SkuStoresInventoryMessage actualMsgSaved = msgCaptor.getValue();
    SkuStoresInventoryMessage actualMsgAnnounced = announcementCaptor.getValue();
    assertThat("Processed message and announceMessage should be same", actualMsgSaved, equalTo(actualMsgAnnounced));

    StoreInventory sku1Store1Inventory = actualMsgSaved.getSkuStores().get(0).getStoreInventories().get(0);
    assertThat("StoreId should match", sku1Store1Inventory.getStoreId(), equalTo("store1/location1"));
    assertThat("StoreNumber should match", sku1Store1Inventory.getStoreNumber(), equalTo("store1"));
    assertThat("LocationNumber should match", sku1Store1Inventory.getLocationNumber(), equalTo("location1"));
    assertThat("InventoryLevel should match", sku1Store1Inventory.getInvLevel(), equalTo("1"));
    assertThat("Quantity should match", sku1Store1Inventory.getQuantity(), equalTo(100));
    assertThat("BopsQuantity should match", sku1Store1Inventory.getBopsQuantity(), equalTo(35));


  }
  @Pact(provider = "ATG-rabbitmq", consumer = "store-service")
  public MessagePact createPactForEmptySkuStoresOnInventoryUpdatedMessage(MessagePactBuilder builder) {
    PactDslJsonBody body = new PactDslJsonBody();

    body.stringType("id", "product1234");

    body.array("skuStores").object().stringType("skuId", "sku1")
            .array("storeInventories")
            .closeArray()
            .closeObject()
            .object().stringType("skuId", "sku2")
            .array("storeInventories")
            .object()
            .stringType("storeNo", "store1")
            .stringType("storeId", "store1/location1")
            .stringType("locationNumber", "location1")
            .stringType("invLevel", "3")
            .integerType("bopsQty", 50)
            .integerType("qty", 96).closeObject()
            .object()
            .stringType("storeNo", "store2")
            .stringType("storeId", "store2/location2")
            .stringType("locationNumber", "location2")
            .stringType("invLevel", "2")
            .integerType("bopsQty", 8)
            .integerType("qty", 8).closeObject()
            .closeArray()
            .closeObject().closeArray();
    body.stringType("batchId", "SomeGeneratedBatchId").stringValue("eventType", "SKU_STORES_INVENTORY_UPDATED");
    body.object("originTimestampInfo").stringType("SKU_STORES_INVENTORY_UPDATED", "2018-04-19T10:44:34.238").closeObject();


    Map<String, String> metadata = new HashMap<>();
    metadata.put("contentType", "application/json");

    return builder.given("store inventory for product message with eventType SKU_STORES_INVENTORY_UPDATED")
            .expectsToReceive("valid store inventory message for product")
            .withMetadata(metadata)
            .withContent(body)
            .toPact();
  }

  @Test
  @PactVerification(value = "store-service", fragment = "createPactForEmptySkuStoresOnInventoryUpdatedMessage")
  public void shouldReceiveStoreSkuInventoryMessageWhenStoresRemovedFromSku() throws Exception {
    byte[] currentMessage = mockProvider.getMessage();
    when(message.getBody()).thenReturn(currentMessage);
    when(message.getMessageProperties().getType()).thenReturn(SKU_STORES_INVENTORY_UPDATED.name());
    handler.handle(message);
    ArgumentCaptor<SkuStoresInventoryMessage> msgCaptor = ArgumentCaptor.forClass(SkuStoresInventoryMessage.class);
    ArgumentCaptor<SkuStoresInventoryMessage> announcementCaptor = ArgumentCaptor.forClass(SkuStoresInventoryMessage.class);
    verify(service).updateStoreSkuInventory(msgCaptor.capture());
    verify(announcer).announceStoreSkuInventoryForProductMessage(announcementCaptor.capture());
    SkuStoresInventoryMessage actualMsgSaved = msgCaptor.getValue();
    SkuStoresInventoryMessage actualMsgAnnounced = announcementCaptor.getValue();
    assertThat("Processed message and announceMessage should be same", actualMsgSaved, equalTo(actualMsgAnnounced));

    assertThat("SkuInventories for Sku1 should be empty", actualMsgSaved.getSkuStores().get(0).getStoreInventories(), equalTo(Collections.emptyList()));
    assertThat("Announced SkuInventories for Sku1 should be empty", actualMsgAnnounced.getSkuStores().get(0).getStoreInventories(), equalTo(Collections.emptyList()));
  }
}

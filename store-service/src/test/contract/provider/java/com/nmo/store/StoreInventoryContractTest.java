package com.sixthday.store;

import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit.Consumer;
import au.com.dius.pact.provider.junit.PactRunner;
import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.loader.PactBrokerAuth;
import au.com.dius.pact.provider.junit.target.AmqpTarget;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.store.cloud.stream.StoreInventoryBySKUAnnouncer;
import com.sixthday.store.cloud.stream.StoreSkuInventoryAnnouncer;
import com.sixthday.store.config.StoreInventoryBySKUSource;
import com.sixthday.store.data.SkuStoresInventoryMessageBuilder;
import com.sixthday.store.models.storeinventoryindex.SkuStore;
import com.sixthday.store.models.storeinventoryindex.SkuStoresInventoryMessage;
import com.sixthday.store.models.storeinventoryindex.StoreInventory;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryMessage;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.sixthday.store.models.storeinventoryindex.SkuStoresInventoryMessage.EventType.SKU_STORES_INVENTORY_UPDATED;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.mockito.Mockito.verify;

@RunWith(PactRunner.class)
@Provider("store-service")
@Consumer("product-sub-svc")
@PactBroker(protocol = "${pactbroker.protocol}", host = "${pactbroker.hostname}", port = "${pactbroker.port}",
				authentication = @PactBrokerAuth(username = "${pactbroker.username}", password = "${pactbroker.password}"))
public class StoreInventoryContractTest {

	@TestTarget
	public final Target target = new AmqpTarget();

	@Mock
	private MessageChannel messageChannel;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private Source source;

	private StoreSkuInventoryAnnouncer storeSkuInventoryAnnouncer;
	private StoreSkuInventoryMessage message;
	private boolean removed;

  @Mock
  private MessageChannel storeInventoryForSkuChannel;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private StoreInventoryBySKUSource storeInventoryBySKUSource;

  private StoreInventoryBySKUAnnouncer storeInventoryBySKUAnnouncer;
  private SkuStoresInventoryMessage skuStoresInventoryMessage;


	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(source.output()).thenReturn(messageChannel);
    storeSkuInventoryAnnouncer = new StoreSkuInventoryAnnouncer(source);
    Mockito.when(storeInventoryBySKUSource.storeInventoryBySKUChannel()).thenReturn(storeInventoryForSkuChannel);
    storeInventoryBySKUAnnouncer = new StoreInventoryBySKUAnnouncer();
    storeInventoryBySKUAnnouncer.setMessageSource(storeInventoryBySKUSource);
	}

	@State("store sku inventory was deleted")
	public void setupRemovedInventory() {
		removed = true;
		message = generateStoreSkuInventoryMessage();
	}

	@PactVerifyProvider("StoreSkuInventoryMessage with removed set to true")
	public String testRemovedStoreSkuInventory() throws JsonProcessingException {
		ArgumentCaptor<Message> argumentCaptor = ArgumentCaptor.forClass(Message.class);

		storeSkuInventoryAnnouncer.announceStoreSkuInventoryUpdate(message, removed);

		verify(messageChannel).send(argumentCaptor.capture());
		return new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(argumentCaptor.getValue().getPayload());
	}

	@State("store sku inventory was added")
	public void setupAddedInventory() {
		removed = false;
		message = generateStoreSkuInventoryMessage();
	}

	@PactVerifyProvider("StoreSkuInventoryMessage with removed set to false")
	public String testAddedStoreSkuInventory() throws JsonProcessingException {
		ArgumentCaptor<Message> argumentCaptor = ArgumentCaptor.forClass(Message.class);

		storeSkuInventoryAnnouncer.announceStoreSkuInventoryUpdate(message, removed);

		verify(messageChannel).send(argumentCaptor.capture());
		return new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(argumentCaptor.getValue().getPayload());
	}

	@NotNull
	private StoreSkuInventoryMessage generateStoreSkuInventoryMessage() {
		StoreSkuInventoryMessage storeSkuInventoryMessage = new StoreSkuInventoryMessage();
		storeSkuInventoryMessage.setId("id");
		storeSkuInventoryMessage.setSkuId("sku1234567");
		storeSkuInventoryMessage.setStoreNumber("storeNumber");
		storeSkuInventoryMessage.setStoreId("store1");
		storeSkuInventoryMessage.setLocationNumber("locationNumber");
		storeSkuInventoryMessage.setInventoryLevelCode("inventoryLevel");
		storeSkuInventoryMessage.setQuantity(1);
		storeSkuInventoryMessage.setBopsQuantity(1);
		storeSkuInventoryMessage.setProductIds(asList("prod1234"));
		return storeSkuInventoryMessage;
	}

  @State("store sku inventory was updated for product")
  public void setupStoreSkuInventoryUpdatedForProduct() {
    skuStoresInventoryMessage = generateSkuStoresInventoryMessageWithAllStoresForSkus();
  }

  @PactVerifyProvider("valid sku stores inventory message with all stores inventory data")
  public String testStoresSkuInventoryMessageIncludesAllStoresForAllSkus() throws JsonProcessingException {
    ArgumentCaptor<Message> argumentCaptor = ArgumentCaptor.forClass(Message.class);

    storeInventoryBySKUAnnouncer.announceStoreSkuInventoryForProductMessage(skuStoresInventoryMessage);

    verify(storeInventoryForSkuChannel).send(argumentCaptor.capture());
    return new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(argumentCaptor.getValue().getPayload());
  }

  @State("SkuStoresInventoryMessage including all stores for all skus with eventType SKU_STORES_INVENTORY_UPDATED")
	public void setupSkuStoresInventoryMessageIncludesAllStoresInventoryForAllSkus(){
		skuStoresInventoryMessage = generateSkuStoresInventoryMessageWithAllStoresForSkus();
	}

  @NotNull
  private SkuStoresInventoryMessage generateSkuStoresInventoryMessageWithAllStoresForSkus() {
    SkuStoresInventoryMessageBuilder builder = new SkuStoresInventoryMessageBuilder();
    builder.withBatchId("GeneratedBatchId").withProductId("prod1234").withEventType(SKU_STORES_INVENTORY_UPDATED)
            .withOriginTimestampInfo(Collections.singletonMap("SKU_STORES_INVENTORY_UPDATED", "GeneratedOriginTimestamp")).withDataPoints(Collections.singletonMap("productId", "product1234"));
    List<SkuStore> skuStores = IntStream.range(0, 2).mapToObj(i -> {
      SkuStore sku = new SkuStore();
      sku.setStoreInventories(IntStream.range(0, 2).mapToObj(k -> {
        StoreInventory inventory = new StoreInventory();
        inventory.setStoreId("store" + k + "/location" + k);
        inventory.setStoreNumber("store" + k);
        inventory.setLocationNumber("location" + k);
        return inventory;
      }).collect(Collectors.toList()));
      sku.setSkuId("sku" + i);
      return sku;
    }).collect(Collectors.toList());
    StoreInventory s1s1 = skuStores.get(0).getStoreInventories().get(0);
    s1s1.setBopsQuantity(35);
    s1s1.setQuantity(100);
    s1s1.setInvLevel("1");

    StoreInventory s1s2 = skuStores.get(0).getStoreInventories().get(1);
    s1s2.setBopsQuantity(50);
    s1s2.setQuantity(80);
    s1s2.setInvLevel("1");

    StoreInventory s2s1 = skuStores.get(1).getStoreInventories().get(0);
    s2s1.setBopsQuantity(50);
    s2s1.setQuantity(96);
    s2s1.setInvLevel("3");

    StoreInventory s2s2 = skuStores.get(1).getStoreInventories().get(1);
    s2s2.setBopsQuantity(8);
    s2s2.setQuantity(8);
    s2s2.setInvLevel("2");

    builder.withSkuStores(skuStores);
    return builder.build();
  }

  @PactVerifyProvider("valid sku stores inventory message with removing all stores inventory data for a sku")
  public String testStoresSkuInventoryMessageIncludesNoStoresForASku() throws JsonProcessingException {
    ArgumentCaptor<Message> argumentCaptor = ArgumentCaptor.forClass(Message.class);

    storeInventoryBySKUAnnouncer.announceStoreSkuInventoryForProductMessage(skuStoresInventoryMessage);

    verify(storeInventoryForSkuChannel).send(argumentCaptor.capture());
    return new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(argumentCaptor.getValue().getPayload());
  }

  @State("SkuStoresInventoryMessage removing all stores invenotry for a sku with eventType SKU_STORES_INVENTORY_UPDATED")
  public void setupSkuStoresInventoryMessageIncludesNoStoresInventoryForASku(){
    skuStoresInventoryMessage = generateSkuStoresInventoryMessageWithNoStoresForASku();
  }

  @NotNull
  private SkuStoresInventoryMessage generateSkuStoresInventoryMessageWithNoStoresForASku() {
    SkuStoresInventoryMessageBuilder builder = new SkuStoresInventoryMessageBuilder();
    builder.withBatchId("GeneratedBatchId").withProductId("prod1234").withEventType(SKU_STORES_INVENTORY_UPDATED)
            .withOriginTimestampInfo(Collections.singletonMap("SKU_STORES_INVENTORY_UPDATED", "GeneratedOriginTimestamp")).withDataPoints(Collections.singletonMap("productId", "product1234"));
    List<SkuStore> skuStores = IntStream.range(0, 2).mapToObj(i -> {
      SkuStore sku = new SkuStore();
      sku.setStoreInventories(IntStream.range(0, 2).mapToObj(k -> {
        StoreInventory inventory = new StoreInventory();
        inventory.setStoreId("store" + k + "/location" + k);
        inventory.setStoreNumber("store" + k);
        inventory.setLocationNumber("location" + k);
        return inventory;
      }).collect(Collectors.toList()));
      sku.setSkuId("sku" + i);
      return sku;
    }).collect(Collectors.toList());

    skuStores.get(0).setStoreInventories(Collections.emptyList());

    StoreInventory s2s1 = skuStores.get(1).getStoreInventories().get(0);
    s2s1.setBopsQuantity(50);
    s2s1.setQuantity(96);
    s2s1.setInvLevel("3");

    StoreInventory s2s2 = skuStores.get(1).getStoreInventories().get(1);
    s2s2.setBopsQuantity(8);
    s2s2.setQuantity(8);
    s2s2.setInvLevel("2");

    builder.withSkuStores(skuStores);
    return builder.build();
  }
}

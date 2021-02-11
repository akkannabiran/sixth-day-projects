package com.sixthday.store;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.MessagePactProviderRule;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.model.v3.messaging.MessagePact;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryDocument;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryMessage;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryMessage.EventType;
import com.sixthday.store.services.StoreSkuInventorySyncService;
import com.sixthday.store.util.sixthdayMDCAdapter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.amqp.core.Message;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;

@RunWith(MockitoJUnitRunner.class)
public class StoreSkuInventoryMessageReceiverContractTest {

    @Mock
    private StoreSkuInventorySyncService storeSkuInventorySyncService;

    @Mock
    private sixthdayMDCAdapter mdc;

    @Mock(answer=Answers.RETURNS_DEEP_STUBS)
    private Message message;

    @InjectMocks
    private StoreSkuInventoryMessageReceiver storeSkuInventoryMessageReceiver;

    @Rule
    public MessagePactProviderRule mockProvider = new MessagePactProviderRule("ATG-rabbitmq", this);

    @Pact(provider = "ATG-rabbitmq", consumer = "store-service")
    public MessagePact createPactWithValidUpsertMessage(MessagePactBuilder builder) {
        PactDslJsonBody body = new PactDslJsonBody();

        body.stringType("id");
        body.stringType("skuId");
        body.stringType("storeNumber");
        body.stringType("storeId");
        body.stringType("locationNumber");
        body.stringType("inventoryLevelCode");
        body.integerType("quantity");
        body.integerType("bopsQuantity");

        body.stringValue("eventType", "STORE_SKU_INVENTORY_UPSERT");

        Map<String, String> metadata = new HashMap<>();
        metadata.put("contentType", "application/json");

        return builder.given("store sku inventory message with eventType UPSERT")
                .expectsToReceive("valid store sku inventory message")
                .withMetadata(metadata)
                .withContent(body)
                .toPact();
    }

    @Pact(provider = "ATG-rabbitmq", consumer = "store-service")
    public MessagePact createPactWithValidDeleteMessage(MessagePactBuilder builder) {
        PactDslJsonBody body = new PactDslJsonBody();

        body.stringType("id");

        body.stringValue("eventType", "STORE_SKU_INVENTORY_DELETE");

        Map<String, String> metadata = new HashMap<>();
        metadata.put("contentType", "application/json");

        return builder.given("store sku inventory message with eventType DELETE")
                .expectsToReceive("valid store sku inventory message")
                .withMetadata(metadata)
                .withContent(body)
                .toPact();
    }

    @Test
    @PactVerification(value = "ATG-rabbitmq", fragment = "createPactWithValidUpsertMessage")
    public void responseWhenValidUpsertEvent() throws Exception {
        byte[] currentMessage = mockProvider.getMessage();
        when(message.getBody()).thenReturn(currentMessage);
        when(message.getMessageProperties().getType()).thenReturn(EventType.STORE_SKU_INVENTORY_UPSERT.name());
        storeSkuInventoryMessageReceiver.onMessage(message, null);
        verify(storeSkuInventorySyncService).updateStoreSkuInventory(any(StoreSkuInventoryDocument.class), any(StoreSkuInventoryMessage.class));
    }

    @Test
    @PactVerification(value = "ATG-rabbitmq", fragment = "createPactWithValidDeleteMessage")
    public void responseWhenValidDeleteEvent() throws Exception {
        byte[] currentMessage = mockProvider.getMessage();
        when(message.getBody()).thenReturn(currentMessage);
        when(message.getMessageProperties().getType()).thenReturn(EventType.STORE_SKU_INVENTORY_DELETE.name());
        storeSkuInventoryMessageReceiver.onMessage(message, null);
        verify(storeSkuInventorySyncService).updateStoreSkuInventory(any(StoreSkuInventoryDocument.class), any(StoreSkuInventoryMessage.class));
    }
}

package com.sixthday.store;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.MessagePactProviderRule;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.model.v3.messaging.MessagePact;
import com.sixthday.store.models.storeindex.StoreDocument;
import com.sixthday.store.models.storeindex.StoreMessage;
import com.sixthday.store.services.StoreSyncService;
import com.sixthday.store.util.sixthdayMDCAdapter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public class StoreMessageReceiverContractTest {

    @Mock
    private StoreSyncService storeSyncService;

    @Mock
    private sixthdayMDCAdapter mdc;

    @Mock
    private Message message;

    @InjectMocks
    private StoreMessageReceiver storeMessageReceiver;

    @Rule
    public MessagePactProviderRule mockProvider = new MessagePactProviderRule("ATG-rabbitmq", this);

    @Pact(provider = "ATG-rabbitmq", consumer = "store-service")
    public MessagePact createPactWithValidMessage(MessagePactBuilder builder) {
        PactDslJsonBody body = new PactDslJsonBody();

        body.stringType("id", "id1");
        body.stringType("storeNumber", "storeNumber1");
        body.stringType("storeName", "store name");
        body.stringType("addressLine1", "2910");
        body.stringType("addressLine2", "7th Avenue ");
        body.stringType("city", "Madison");
        body.stringType("state", "New Jersey");
        body.stringType("zipCode", "50019");
        body.stringType("phoneNumber", "280-897-4555");
        body.stringType("storeHours", "9:00 AM - 7:00 PM");
        body.stringType("storeDescription", "Downtown store");
        body.booleanType("displayable");
        body.booleanType("eligibleForBOPS");

        body.eachLike("events", 2)
                .stringType("eventId")
                .stringType("eventName")
                .stringType("eventTypeId")
                .stringType("eventDescription")
                .date("eventStartDate")
                .date("eventEndDate")
                .closeObject()
                .closeArray();

        body.stringValue("eventType", "STORE_UPSERT");
        body.stringValue("messageType", "message_type");

        Map<String, String> metadata = new HashMap<>();
        metadata.put("contentType", "application/json");

        return builder.given("store information message with valid eventType")
                .expectsToReceive("valid store information message")
                .withMetadata(metadata)
                .withContent(body)
                .toPact();
    }

    @Test
    @PactVerification(value = "ATG-rabbitmq", fragment = "createPactWithValidMessage")
    public void responseWhenValidEvent() throws Exception {
        byte[] currentMessage = mockProvider.getMessage();
        when(message.getBody()).thenReturn(currentMessage);
        storeMessageReceiver.onMessage(message, null);
        verify(storeSyncService).upsertStore(any(StoreDocument.class), any(StoreMessage.EventType.class));
    }
}

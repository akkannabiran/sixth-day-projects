package com.sixthday.store;

import com.sixthday.store.handlers.SkuStoresInventoryMessageHandler;
import static com.sixthday.store.models.storeinventoryindex.SkuStoresInventoryMessage.EventType.SKU_STORES_INVENTORY_UPDATED;
import com.sixthday.store.util.MDCUtils;
import com.sixthday.storeinventory.receiver.SkuStoresInventoryMessageReceiver;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;


@RunWith(PowerMockRunner.class)
@PrepareForTest(value = MDCUtils.class)
@PowerMockIgnore("javax.management.*")
public class SkuStoresInventoryMessageReceiverTest {

  private static final String STORE_INVENTORY_FOR_PRODUCT_MSG =
          "{ \"id\": \"productId12345\",\n" + "  \"skuStores\":[\n" + "    {\n" + "      \"skuId\": \"sku159531036\",\n"
                  + "      \"storeInventories\":[\n" + "        {\n" + "          \"storeNumber\": \"02\",\n" + "          \"storeId\": \"02/BL\",\n"
                  + "          \"locationNumber\": \"1106\",\n" + "          \"bopsQuantity\": 2,\n" + "          \"quantity\": 0,\n"
                  + "          \"inventoryLevel\": \"50\"\n" + "        },\n" + "        {\n" + "          \"storeNumber\": \"03\",\n"
                  + "          \"storeId\": \"03/BL\",\n" + "          \"locationNumber\": \"1106\",\n" + "          \"bopsQuantity\": 2,\n"
                  + "          \"quantity\": 0,\n" + "          \"inventoryLevel\": \"51\"\n" + "        },\n" + "        {\n"
                  + "          \"storeNumber\": \"04\",\n" + "          \"storeId\": \"04/BL\",\n" + "          \"locationNumber\": \"1106\",\n"
                  + "          \"bopsQuantity\": 2,\n" + "          \"quantity\": 0,\n" + "          \"inventoryLevel\": \"90\"\n" + "        }\n"
                  + "      ]\n" + "    },\n" + "    {\n" + "      \"id\": \"sku159531037\",\n" + "      \"storeInventories\":[\n" + "        {\n"
                  + "          \"storeNumber\": \"06\",\n" + "          \"storeId\": \"06/BL\",\n" + "          \"locationNumber\": \"1187\",\n"
                  + "          \"bopsQuantity\": 2,\n" + "          \"quantity\": 1,\n" + "          \"inventoryLevel\": \"49\"\n" + "        },\n"
                  + "        {\n" + "          \"storeNumber\": \"07\",\n" + "          \"storeId\": \"07/BL\",\n"
                  + "          \"locationNumber\": \"1187\",\n" + "          \"bopsQuantity\": 2,\n" + "          \"quantity\": 8,\n"
                  + "          \"inventoryLevel\": \"87\"\n" + "        },\n" + "        {\n" + "          \"storeNumber\": \"08\",\n"
                  + "          \"storeId\": \"08/BL\",\n" + "          \"locationNumber\": \"1187\",\n" + "          \"bopsQuantity\": 2,\n"
                  + "          \"quantity\": 10,\n" + "          \"inventoryLevel\": \"90\"\n" + "        }\n" + "      ]\n" + "    }\n" + "  ]\n"
                  + "}\n";

  @Mock
  private SkuStoresInventoryMessageHandler skuStoresInventoryMessageHandler;

  @InjectMocks
  private SkuStoresInventoryMessageReceiver skuStoresInventoryMessageReceiver;

  @BeforeClass
  public static void setLoggerContextSelector() {
    System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.selector.BasicContextSelector");
  }

  private Message buildMessage(String messageBody) {

    return MessageBuilder.withBody(messageBody.getBytes()).setType(SKU_STORES_INVENTORY_UPDATED.name()).build();
  }

  @Test
  public void shouldCallHandlerWhenMessageReceived() {
    ArgumentCaptor<Message> msgCaptor = ArgumentCaptor.forClass(Message.class);
    Message message = buildMessage(STORE_INVENTORY_FOR_PRODUCT_MSG);
    skuStoresInventoryMessageReceiver.onMessage(message, null);

    verify(skuStoresInventoryMessageHandler).handle(msgCaptor.capture());
    assertThat("Should invoke service with sku inventory for product object", msgCaptor.getValue(), equalTo(message));
  }

}

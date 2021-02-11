package com.sixthday.store;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryDocument;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryMessage;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryMessage.EventType;

@RunWith(MockitoJUnitRunner.class)
public class StoreSkuInventoryMessageMapperTest {
    private StoreSkuInventoryMessageMapper storeSkuInventoryMessageMapper = new StoreSkuInventoryMessageMapper();

    @Test
    public void shouldMapStoreMessageAttributesToStoreInventoryDocument() {
    	StoreSkuInventoryMessage expectedStoreSkuInventoryMessage = new StoreSkuInventoryMessage();
        expectedStoreSkuInventoryMessage.setId("sku92740103:01");
        expectedStoreSkuInventoryMessage.setStoreNumber("01");
        expectedStoreSkuInventoryMessage.setStoreId("01/DT");
        expectedStoreSkuInventoryMessage.setLocationNumber("1000");
        expectedStoreSkuInventoryMessage.setQuantity(1);
        expectedStoreSkuInventoryMessage.setEventType(EventType.STORE_SKU_INVENTORY_UPSERT);
        expectedStoreSkuInventoryMessage.setBopsQuantity(8);
        expectedStoreSkuInventoryMessage.setInventoryLevelCode("2");
        expectedStoreSkuInventoryMessage.setSkuId("sku92740103");

        StoreSkuInventoryDocument storeSkuInventoryDocument = storeSkuInventoryMessageMapper.map(expectedStoreSkuInventoryMessage);
        
        assertThat(storeSkuInventoryDocument.getId(), is("sku92740103:01"));
        assertThat(storeSkuInventoryDocument.getStoreNumber(), is("01"));
        assertThat(storeSkuInventoryDocument.getStoreId(), is("01/DT"));
        assertThat(storeSkuInventoryDocument.getLocationNumber(), is("1000"));
        assertThat(storeSkuInventoryDocument.getQuantity(), is(1));
        assertThat(storeSkuInventoryDocument.getBopsQuantity(), is(8));
        assertThat(storeSkuInventoryDocument.getInventoryLevelCode(), is("2"));
        assertThat(storeSkuInventoryDocument.getSkuId(), is("sku92740103"));
    }
}

package com.sixthday.store.models;

import com.sixthday.store.data.StoreSkuInventoryDocumentBuilder;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryDocument;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;


public class SkuAvailabilityInfoTest {

    private static final String NOT_AVAILABLE_TODAY = "Not Available Today";
    private final String AVAILABLE_FOR_PICKUP_TODAY = "Available for Pickup Today";
    private static final String LIMITED_STOCK = "Available for Pickup Today. Limited Stock";
    private static final String PICK_UP_TODAY = "Add to Cart for Pickup Today";
    private static final String PICK_UP_IN_TWO_THREE_DAYS = "Add to Cart for Pickup in 2-3 Days";

    @Test
    public void shouldSetStatusNotAvailableWhenUserRequestedQuantityIsGreaterThanPresentQuantity() throws Exception {
        StoreSkuInventoryDocument storeSkuInventoryDocument = new StoreSkuInventoryDocumentBuilder().withStoreNumber("123")
                .withInventoryLevelCode("1").withQuantity(1).build();

        SkuAvailabilityInfo skuAvailabilityInfo = new SkuAvailabilityInfo(storeSkuInventoryDocument, 2);

        assertThat(skuAvailabilityInfo.getAvailabilityStatus(),is(NOT_AVAILABLE_TODAY));
        assertFalse(skuAvailabilityInfo.isInventoryAvailable());
        assertThat(skuAvailabilityInfo.getAddToCartMessage(), is(PICK_UP_IN_TWO_THREE_DAYS));
    }

    @Test
    public void shouldSetStatusNotAvailableWhenInventoryLevelIsZero() throws Exception {
        StoreSkuInventoryDocument storeSkuInventoryDocument = new StoreSkuInventoryDocumentBuilder().withStoreNumber("123")
                .withInventoryLevelCode("0").withQuantity(1).build();

        SkuAvailabilityInfo skuAvailabilityInfo = new SkuAvailabilityInfo(storeSkuInventoryDocument, 2);

        assertThat(skuAvailabilityInfo.getAvailabilityStatus(),is(NOT_AVAILABLE_TODAY));
        assertFalse(skuAvailabilityInfo.isInventoryAvailable());
        assertThat(skuAvailabilityInfo.getAddToCartMessage(), is(PICK_UP_IN_TWO_THREE_DAYS));
    }

    @Test
    public void shouldSetStatusNotAvailableWhenInventoryLevelIsNull() throws Exception {
        StoreSkuInventoryDocument storeSkuInventoryDocument = new StoreSkuInventoryDocumentBuilder().withStoreNumber("123")
                .withInventoryLevelCode(null).withQuantity(0).build();

        SkuAvailabilityInfo skuAvailabilityInfo = new SkuAvailabilityInfo(storeSkuInventoryDocument, 2);

        assertThat(skuAvailabilityInfo.getAvailabilityStatus(),is(NOT_AVAILABLE_TODAY));
        assertFalse(skuAvailabilityInfo.isInventoryAvailable());
        assertThat(skuAvailabilityInfo.getAddToCartMessage(), is(PICK_UP_IN_TWO_THREE_DAYS));
    }

    @Test
    public void shouldSetStatusNotAvailableWhenInventoryLevelIsBlank() throws Exception {
        StoreSkuInventoryDocument storeSkuInventoryDocument = new StoreSkuInventoryDocumentBuilder().withStoreNumber("123")
                .withInventoryLevelCode(" ").withQuantity(0).build();

        SkuAvailabilityInfo skuAvailabilityInfo = new SkuAvailabilityInfo(storeSkuInventoryDocument, 2);

        assertThat(skuAvailabilityInfo.getAvailabilityStatus(),is(NOT_AVAILABLE_TODAY));
        assertFalse(skuAvailabilityInfo.isInventoryAvailable());
        assertThat(skuAvailabilityInfo.getAddToCartMessage(), is(PICK_UP_IN_TWO_THREE_DAYS));
    }

    @Test
    public void shouldSetStatusNotAvailableWhenQuantityIsNegative() throws Exception {
        StoreSkuInventoryDocument storeSkuInventoryDocument = new StoreSkuInventoryDocumentBuilder().withStoreNumber("123")
                .withInventoryLevelCode(" ").withQuantity(-1).build();

        SkuAvailabilityInfo skuAvailabilityInfo = new SkuAvailabilityInfo(storeSkuInventoryDocument, 2);

        assertThat(skuAvailabilityInfo.getAvailabilityStatus(),is(NOT_AVAILABLE_TODAY));
        assertFalse(skuAvailabilityInfo.isInventoryAvailable());
        assertThat(skuAvailabilityInfo.getAddToCartMessage(), is(PICK_UP_IN_TWO_THREE_DAYS));
    }

    @Test
    public void shouldSetAvailableForPickupTodayWhenInventoryLevelIsOne() throws Exception {
        StoreSkuInventoryDocument storeSkuInventoryDocument = new StoreSkuInventoryDocumentBuilder().withStoreNumber("123")
                .withInventoryLevelCode("1").withQuantity(1).build();

        SkuAvailabilityInfo skuAvailabilityInfo = new SkuAvailabilityInfo(storeSkuInventoryDocument, 1);

        assertThat(skuAvailabilityInfo.getAvailabilityStatus(),is(AVAILABLE_FOR_PICKUP_TODAY));
        assertTrue(skuAvailabilityInfo.isInventoryAvailable());
        assertThat(skuAvailabilityInfo.getAddToCartMessage(), is(PICK_UP_TODAY));
    }

    @Test
    public void shouldSetAvailableForPickupTodayWhenInventoryLevelIsNullAndQuantityIsGreaterThanFour() throws Exception {
        StoreSkuInventoryDocument storeSkuInventoryDocument = new StoreSkuInventoryDocumentBuilder().withStoreNumber("123")
                .withInventoryLevelCode(" ").withQuantity(4).build();

        SkuAvailabilityInfo skuAvailabilityInfo = new SkuAvailabilityInfo(storeSkuInventoryDocument, 1);

        assertThat(skuAvailabilityInfo.getAvailabilityStatus(),is(AVAILABLE_FOR_PICKUP_TODAY));
        assertTrue(skuAvailabilityInfo.isInventoryAvailable());
        assertThat(skuAvailabilityInfo.getAddToCartMessage(), is(PICK_UP_TODAY));
    }

    @Test
    public void shouldSetLimitedStockWhenInventoryLevelIsTwo() throws Exception {
        StoreSkuInventoryDocument storeSkuInventoryDocument = new StoreSkuInventoryDocumentBuilder().withStoreNumber("123")
                .withInventoryLevelCode("2").withQuantity(2).build();

        SkuAvailabilityInfo skuAvailabilityInfo = new SkuAvailabilityInfo(storeSkuInventoryDocument, 1);

        assertThat(skuAvailabilityInfo.getAvailabilityStatus(),is(LIMITED_STOCK));
        assertTrue(skuAvailabilityInfo.isInventoryAvailable());
        assertThat(skuAvailabilityInfo.getAddToCartMessage(), is(PICK_UP_TODAY));
    }

    @Test
    public void shouldSetLimitedStockWhenInventoryLevelIsNullAndQuantityIsBetweenOneAndThree() throws Exception {
        StoreSkuInventoryDocument storeSkuInventoryDocument = new StoreSkuInventoryDocumentBuilder().withStoreNumber("123")
                .withInventoryLevelCode("2").withQuantity(3).build();

        SkuAvailabilityInfo skuAvailabilityInfo = new SkuAvailabilityInfo(storeSkuInventoryDocument, 1);

        assertThat(skuAvailabilityInfo.getAvailabilityStatus(),is(LIMITED_STOCK));
        assertTrue(skuAvailabilityInfo.isInventoryAvailable());
        assertThat(skuAvailabilityInfo.getAddToCartMessage(), is(PICK_UP_TODAY));
    }

    @Test
    public void shouldSetSkuNotAvailableWhenAllConditionsFail() throws Exception {
        StoreSkuInventoryDocument storeSkuInventoryDocument = new StoreSkuInventoryDocumentBuilder().withStoreNumber("123")
                .withInventoryLevelCode(null).withQuantity(0).build();

        SkuAvailabilityInfo skuAvailabilityInfo = new SkuAvailabilityInfo(storeSkuInventoryDocument, 1);

        assertThat(skuAvailabilityInfo.getAvailabilityStatus(),is(NOT_AVAILABLE_TODAY));
        assertFalse(skuAvailabilityInfo.isInventoryAvailable());
        assertThat(skuAvailabilityInfo.getAddToCartMessage(), is(PICK_UP_IN_TWO_THREE_DAYS));
    }

}

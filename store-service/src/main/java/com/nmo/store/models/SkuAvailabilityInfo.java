package com.sixthday.store.models;

import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryDocument;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;

@Getter
public class SkuAvailabilityInfo {

    public static final String NOT_AVAILABLE_TODAY = "Not Available Today";
    public static final String AVAILABLE_FOR_PICKUP_TODAY = "Available for Pickup Today";
    public static final String LIMITED_STOCK = "Available for Pickup Today. Limited Stock";
    public static final String PICK_UP_TODAY = "Add to Cart for Pickup Today";
    public static final String PICK_UP_IN_TWO_THREE_DAYS = "Add to Cart for Pickup in 2-3 Days";
    public static final String NOT_AVAILABLE = "0";
    public static final String AVAILABLE = "1";
    public static final String LIMITED_AVAILABILITY = "2";

    private String availabilityStatus;
    private boolean inventoryAvailable;
    private String addToCartMessage;
    private String storeNumber;

    public SkuAvailabilityInfo(StoreSkuInventoryDocument storeSkuInventoryDocument, Integer userRequestedQuantity) {
        setAvailability(storeSkuInventoryDocument, userRequestedQuantity);
    }

    //TODO: Clean this up. Some of these methods can move to the store document.
    private void setAvailability(StoreSkuInventoryDocument document, Integer userRequestedQuantity) {
        if (userRequestedQuantity > document.getQuantity() || StringUtils.equals(document.getInventoryLevelCode(), NOT_AVAILABLE) ||
                (StringUtils.isBlank(document.getInventoryLevelCode()) && document.getQuantity() <= 0))
            skuAvailabilityInfo(NOT_AVAILABLE_TODAY, false, document.getStoreNumber(), PICK_UP_IN_TWO_THREE_DAYS);
        else if (StringUtils.equals(document.getInventoryLevelCode(), AVAILABLE) ||
                (StringUtils.isBlank(document.getInventoryLevelCode()) && document.getQuantity() >= 4))
            skuAvailabilityInfo(AVAILABLE_FOR_PICKUP_TODAY, true, document.getStoreNumber(),PICK_UP_TODAY);
        else if (StringUtils.equals(document.getInventoryLevelCode(), LIMITED_AVAILABILITY) ||
                (StringUtils.isBlank(document.getInventoryLevelCode()) && document.getQuantity() > 0 && document.getQuantity() < 4))
            skuAvailabilityInfo(LIMITED_STOCK, true, document.getStoreNumber(),PICK_UP_TODAY);
        else
            skuAvailabilityInfo(NOT_AVAILABLE_TODAY, false, document.getStoreNumber(), PICK_UP_IN_TWO_THREE_DAYS);
    }

    private void skuAvailabilityInfo(String availabilityStatus, boolean inventoryAvailable, String storeNumber, String addToCartMessage) {
        this.availabilityStatus = availabilityStatus;
        this.inventoryAvailable = inventoryAvailable;
        this.storeNumber = storeNumber;
        this.addToCartMessage = addToCartMessage;
    }
}
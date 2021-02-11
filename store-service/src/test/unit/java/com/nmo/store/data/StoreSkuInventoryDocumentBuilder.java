package com.sixthday.store.data;


import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryDocument;

public class StoreSkuInventoryDocumentBuilder {
    private String id = UUID.randomUUID().toString();
    private String skuId = "SKU_ID";
    private String storeNumber = "STORE_NUMBER";
    private String storeId = "STORE_ID";
    private String locationNumber = "LOCATION_NUMBER";
    private String inventoryLevelCode = "INVENTORY_LEVEL_CODE";
    private int quantity = 1;
    private int bopsQuantity = 1;
    private List<String> productIds = Arrays.asList("PRODUCT_ID1");

    public StoreSkuInventoryDocument build() {
        return new StoreSkuInventoryDocument(
                id,
                skuId,
                storeNumber,
                storeId,
                locationNumber,
                inventoryLevelCode,
                quantity,
                bopsQuantity,
                productIds
        );
    }

    public StoreSkuInventoryDocumentBuilder withStoreNumber(String storeNumber) {
        this.storeNumber = storeNumber;
        return this;
    }

    public StoreSkuInventoryDocumentBuilder withInventoryLevelCode(String inventoryLevelCode) {
        this.inventoryLevelCode = inventoryLevelCode;
        return this;
    }

    public StoreSkuInventoryDocumentBuilder withQuantity(Integer quantity) {
        this.quantity = quantity;
        return this;
    }
    
    public StoreSkuInventoryDocumentBuilder withProductList(List<String> productIds) {
      this.productIds = productIds;
      return this;
    }
}
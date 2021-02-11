package com.sixthday.store.data;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryAnnouncement;

public class StoreSkuInventoryAnnoucementBuilder {
  private List<String> productIds = Arrays.asList("PRODUCT_ID1");
  private String skuId = "SKU_ID";
  private String storeNumber = "STORE_NUMBER";
  private String inventoryLevel = "INVENTORY_LEVEL_CODE";
  private int quantity = 1;
  private boolean removed = false;
  private String batchId = null;
  private Map<String, String> originTimestampInfo = null;
  private Map<String, Object> dataPoints = null;
  
  public StoreSkuInventoryAnnouncement build() {
    return new StoreSkuInventoryAnnouncement(productIds, skuId, storeNumber, inventoryLevel, quantity, removed, batchId, originTimestampInfo, dataPoints);
  }
  
  public StoreSkuInventoryAnnoucementBuilder withProductIds(List<String> productIds) {
    this.productIds = productIds;
    return this;
  }
  
  public StoreSkuInventoryAnnoucementBuilder withSkudId(String skuId) {
    this.skuId = skuId;
    return this;
  }
  
  public StoreSkuInventoryAnnoucementBuilder withStoreNumber(String storeNumber) {
    this.storeNumber = storeNumber;
    return this;
  }
  
  public StoreSkuInventoryAnnoucementBuilder withInventoryLevel(String inventoryLevel) {
    this.inventoryLevel = inventoryLevel;
    return this;
  }
  
  public StoreSkuInventoryAnnoucementBuilder withQuantity(int qty) {
    this.quantity = qty;
    return this;
  }
  
  public StoreSkuInventoryAnnoucementBuilder withRemoved(boolean removed) {
    this.removed = removed;
    return this;
  }
  public StoreSkuInventoryAnnoucementBuilder withBatchId(String batchId) {
    this.batchId = batchId;
    return this;
  }
  public StoreSkuInventoryAnnoucementBuilder withOriginTimestampInfo(Map<String, String> originTimestampInfo) {
    this.originTimestampInfo = originTimestampInfo;
    return this;
  }
  public StoreSkuInventoryAnnoucementBuilder withDataPoints(Map<String, Object> dataPoints) {
    this.dataPoints = dataPoints;
    return this;
  }
}

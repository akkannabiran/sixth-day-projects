package com.sixthday.store.data;

import com.sixthday.store.models.StoreInventoryItem;

public class StoreInventoryItemBuilder {
  private String storeNumber = "STORE1";
  private String storeId = "STORE1/LOCATION1";
  private String locationNumber = "LOCATION1";
  private String inventoryLevel = "01";
  private int quantity = 999;
  private int bopsQuantity = 99;
  
  public StoreInventoryItem build() {
    StoreInventoryItem doc = new StoreInventoryItem();
    doc.setStoreId(storeId);
    doc.setStoreNumber(storeNumber);
    doc.setLocationNumber(locationNumber);
    doc.setInventoryLevel(inventoryLevel);
    doc.setQuantity(quantity);
    doc.setBopsQuantity(bopsQuantity);
    return doc;
  }

  public StoreInventoryItemBuilder withAll(String storeNumber, String storeId, String locationNumber, String inventoryLevel, int quantity, int bopsQuantity) {
    this.storeNumber = storeNumber;
    this.storeId = storeId;
    this.locationNumber = locationNumber;
    this.inventoryLevel = inventoryLevel;
    this.quantity = quantity;
    this.bopsQuantity = bopsQuantity;
    return  this;
  }

  public StoreInventoryItemBuilder withStoreNumber(String storeNumber) {
    this.storeNumber = storeNumber;
    return this;
  }
  
  public StoreInventoryItemBuilder withStoreId(String storeId) {
    this.storeId = storeId;
    return this;
  }
  
  public StoreInventoryItemBuilder withLocationNumber(String locationNumber) {
    this.locationNumber = locationNumber;
    return this;
  }
  
  public StoreInventoryItemBuilder withInventoryLevel(String inventoryLevel) {
    this.inventoryLevel = inventoryLevel;
    return this;
  }
  
  public StoreInventoryItemBuilder withQuanity(int qty) {
    this.quantity = qty;
    return this;
  }
  
  public StoreInventoryItemBuilder withBopsQuantity(int bopsQty) {
    this.bopsQuantity = bopsQty;
    return this;
  }
}

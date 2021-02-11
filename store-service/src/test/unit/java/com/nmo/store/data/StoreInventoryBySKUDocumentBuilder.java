package com.sixthday.store.data;

import java.util.ArrayList;
import java.util.List;

import com.sixthday.store.models.StoreInventoryBySKUDocument;
import com.sixthday.store.models.StoreInventoryItem;

public class StoreInventoryBySKUDocumentBuilder {
  private String skuId = "SKU-ID";
  private List<StoreInventoryItem> storeInventoryItems = new ArrayList<>();
  
  public StoreInventoryBySKUDocument build() {
    StoreInventoryBySKUDocument doc = new StoreInventoryBySKUDocument();
    doc.setSkuId(skuId);
    doc.setStoreInventoryItems(storeInventoryItems);
    return doc;
  }
  
  public StoreInventoryBySKUDocumentBuilder withSkuId(String skuId) {
    this.skuId = skuId;
    return this;
  }
  
  public StoreInventoryBySKUDocumentBuilder withStoreInventoryItems(List<StoreInventoryItem> storeInventoryItems) {
    this.storeInventoryItems = storeInventoryItems;
    return this;
  }
  
}

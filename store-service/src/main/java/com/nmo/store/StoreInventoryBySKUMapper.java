package com.sixthday.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.sixthday.store.models.storeinventoryindex.SkuStoresInventoryMessage;
import org.springframework.stereotype.Component;

import com.sixthday.store.models.StoreInventoryBySKUDocument;
import com.sixthday.store.models.StoreInventoryItem;

@Component
public class StoreInventoryBySKUMapper {
  
  public List<StoreInventoryBySKUDocument> map(SkuStoresInventoryMessage message) {
    List<StoreInventoryBySKUDocument> documents = new ArrayList<>();
    message.getSkuStores().forEach(storeSkuInventory -> {
      StoreInventoryBySKUDocument doc = new StoreInventoryBySKUDocument();
      doc.setSkuId(storeSkuInventory.getSkuId());
      doc.setStoreInventoryItems(Optional.ofNullable(storeSkuInventory.getStoreInventories()).orElse(new ArrayList<>()).stream().map(inv -> {
        StoreInventoryItem inventoryItem = new StoreInventoryItem();
        inventoryItem.setStoreNumber(inv.getStoreNumber());
        inventoryItem.setStoreId(inv.getStoreId());
        inventoryItem.setLocationNumber(inv.getLocationNumber());
        inventoryItem.setQuantity(inv.getQuantity());
        inventoryItem.setInventoryLevel(inv.getInvLevel());
        inventoryItem.setBopsQuantity(inv.getBopsQuantity());
        return inventoryItem;
      }).collect(Collectors.toList()));
      
      documents.add(doc);
    });
    return documents;
  }
  
}

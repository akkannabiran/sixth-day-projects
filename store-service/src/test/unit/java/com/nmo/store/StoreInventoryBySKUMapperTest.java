package com.sixthday.store;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.sixthday.store.models.storeinventoryindex.SkuStore;
import org.junit.Before;
import org.junit.Test;

import com.sixthday.store.data.SkuStoresInventoryMessageBuilder;
import com.sixthday.store.models.StoreInventoryBySKUDocument;
import com.sixthday.store.models.StoreInventoryItem;
import com.sixthday.store.models.storeinventoryindex.StoreInventory;
import com.sixthday.store.models.storeinventoryindex.SkuStoresInventoryMessage;

public class StoreInventoryBySKUMapperTest {
  
  private StoreInventoryBySKUMapper mapper;
  
  @Before
  public void setup() {
    mapper = new StoreInventoryBySKUMapper();
  }
  
  @Test
  public void shouldMapSkuStoresInventoryMessageToListOfStoreInventoryBySKUDocuments() {
    SkuStore sku1 = new SkuStore();
    StoreInventoryBySKUDocument skuDoc1 = new StoreInventoryBySKUDocument();
    StoreInventoryBySKUDocument skuDoc2 = new StoreInventoryBySKUDocument();
    
    sku1.setSkuId("sku1");
    skuDoc1.setSkuId("sku1");
    sku1.setStoreInventories(IntStream.range(0, 4).mapToObj(i -> {
      StoreInventory inv = new StoreInventory();
      inv.setStoreId("store-" + i + "/location-" + i);
      inv.setStoreNumber("store-" + i);
      inv.setLocationNumber("location-" + i);
      inv.setBopsQuantity(100);
      inv.setQuantity(i + 100);
      inv.setInvLevel("invLvl" + i);
      
      StoreInventoryItem inventoryItem = new StoreInventoryItem();
      inventoryItem.setStoreId("store-" + i + "/location-" + i);
      inventoryItem.setStoreNumber("store-" + i);
      inventoryItem.setLocationNumber("location-" + i);
      inventoryItem.setQuantity(i + 100);
      inventoryItem.setInventoryLevel("invLvl" + i);
      inventoryItem.setBopsQuantity(100);
      skuDoc1.getStoreInventoryItems().add(inventoryItem);
      
      return inv;
    }).collect(Collectors.toList()));
    
    SkuStore sku2 = new SkuStore();
    sku2.setSkuId("sku2");
    skuDoc2.setSkuId("sku2");
    sku2.setStoreInventories(IntStream.range(0, 5).mapToObj(i -> {
      StoreInventory inv = new StoreInventory();
      inv.setStoreId("store-" + i + "/location-" + i);
      inv.setStoreNumber("store-" + i);
      inv.setLocationNumber("location-" + i);
      inv.setBopsQuantity(100);
      inv.setQuantity(i + 100);
      inv.setInvLevel("invLvl" + i);
      
      StoreInventoryItem inventoryItem = new StoreInventoryItem();
      inventoryItem.setStoreId("store-" + i + "/location-" + i);
      inventoryItem.setStoreNumber("store-" + i);
      inventoryItem.setLocationNumber("location-" + i);
      inventoryItem.setQuantity(i + 100);
      inventoryItem.setInventoryLevel("invLvl" + i);
      inventoryItem.setBopsQuantity(100);
      skuDoc2.getStoreInventoryItems().add(inventoryItem);
      
      return inv;
    }).collect(Collectors.toList()));
    
    SkuStoresInventoryMessage message = new SkuStoresInventoryMessageBuilder().withSkuStores(Arrays.asList(sku1, sku2)).build();
    
    List<StoreInventoryBySKUDocument> documents = mapper.map(message);
    List<StoreInventoryBySKUDocument> docs = Arrays.asList(skuDoc1, skuDoc2);
    for (int i = 0; i < docs.size(); i++) {
      assertThat("StoreInvenotryDocument " + (i + 1) + "  should match the store inventory details", documents.get(i), equalTo(docs.get(i)));
    }
    
  }

  @Test
  public void shouldMapSkuStoresInventoryMessageToEmptyListOfStoreInventoryBySKUDocuments() {
    SkuStore sku1 = new SkuStore();
    List<StoreInventoryBySKUDocument> docs = new ArrayList<>();

    sku1.setSkuId("sku1");
    sku1.setStoreInventories(Collections.emptyList());

    SkuStore sku2 = new SkuStore();
    sku2.setSkuId("sku2");
    sku2.setStoreInventories(Collections.emptyList());

    SkuStoresInventoryMessage message = new SkuStoresInventoryMessageBuilder().withSkuStores(Arrays.asList(sku1, sku2)).build();

    List<StoreInventoryBySKUDocument> documents = mapper.map(message);

    for (int i = 0; i < docs.size(); i++) {
      assertThat("StoreInvenotryDocument " + (i + 1) + "  should have empty store inventory details", documents.get(i), equalTo(docs.get(i)));
    }

    sku1.setStoreInventories(null);
    sku2.setStoreInventories(null);
    message = new SkuStoresInventoryMessageBuilder().withSkuStores(Arrays.asList(sku1, sku2)).build();

    documents = mapper.map(message);

    for (int i = 0; i < docs.size(); i++) {
      assertThat("StoreInvenotryDocument " + (i + 1) + "  should have empty store inventory details", documents.get(i), equalTo(docs.get(i)));
    }

  }
  
}

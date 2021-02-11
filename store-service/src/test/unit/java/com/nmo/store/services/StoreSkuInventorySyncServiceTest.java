package com.sixthday.store.services;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import com.sixthday.store.StoreInventoryBySKUMapper;
import com.sixthday.store.data.SkuStoresInventoryMessageBuilder;
import com.sixthday.store.models.StoreInventoryBySKUDocument;
import com.sixthday.store.models.StoreInventoryItem;
import com.sixthday.store.models.storeinventoryindex.SkuStoresInventoryMessage;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryDocument;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryMessage;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryMessage.EventType;
import com.sixthday.store.repository.StoreSkuInventorySyncRepository;
import com.sixthday.store.repository.dynamodb.StoreInventoryBySKURepository;

@RunWith(SpringRunner.class)
public class StoreSkuInventorySyncServiceTest {
  @Mock
  private StoreSkuInventorySyncRepository storeSkuInventorySyncRepository;
  
  @Mock
  private StoreInventoryBySKUMapper storeInventoryBySKUMapper;
  
  @Mock
  private StoreInventoryBySKURepository storeInventoryBySKURepository;
  
  @InjectMocks
  private StoreSkuInventorySyncService storeSkuInventorySyncService;
  
  @Test
  public void shouldCallCreateOrUpdateStoreSkuInventoryWhenSkuInventoryUpsertEvent() throws Exception {
    StoreSkuInventoryDocument storeSkuInventoryDocument = new StoreSkuInventoryDocument();
    StoreSkuInventoryMessage expectedStoreSkuInventoryMessage = new StoreSkuInventoryMessage();
    expectedStoreSkuInventoryMessage.setEventType(EventType.STORE_SKU_INVENTORY_UPSERT);
    storeSkuInventorySyncService.updateStoreSkuInventory(storeSkuInventoryDocument, expectedStoreSkuInventoryMessage);
    
    verify(storeSkuInventorySyncRepository, only()).createOrUpdateStoreSkuInventory(storeSkuInventoryDocument);
    verify(storeSkuInventorySyncRepository, never()).deleteStoreSkuInventory(storeSkuInventoryDocument);
  }
  
  @Test
  public void shouldCallDeleteStoreSkuInventoryWhenSkuInventoryDeleteEvent() throws Exception {
    StoreSkuInventoryDocument storeSkuInventoryDocument = new StoreSkuInventoryDocument();
    StoreSkuInventoryMessage expectedStoreSkuInventoryMessage = new StoreSkuInventoryMessage();
    expectedStoreSkuInventoryMessage.setEventType(EventType.STORE_SKU_INVENTORY_DELETE);
    storeSkuInventorySyncService.updateStoreSkuInventory(storeSkuInventoryDocument, expectedStoreSkuInventoryMessage);
    
    verify(storeSkuInventorySyncRepository, never()).createOrUpdateStoreSkuInventory(storeSkuInventoryDocument);
    verify(storeSkuInventorySyncRepository, only()).deleteStoreSkuInventory(storeSkuInventoryDocument);
  }
  
  @Test
  public void shouldNotCallESWhenThereAreNoEEventType() throws Exception {
    StoreSkuInventoryDocument storeSkuInventoryDocument = new StoreSkuInventoryDocument();
    StoreSkuInventoryMessage expectedStoreSkuInventoryMessage = new StoreSkuInventoryMessage();
    expectedStoreSkuInventoryMessage.setEventType(null);
    storeSkuInventorySyncService.updateStoreSkuInventory(storeSkuInventoryDocument, expectedStoreSkuInventoryMessage);
    
    verify(storeSkuInventorySyncRepository, never()).createOrUpdateStoreSkuInventory(storeSkuInventoryDocument);
    verify(storeSkuInventorySyncRepository, never()).deleteStoreSkuInventory(storeSkuInventoryDocument);
  }
  
  @Test
  public void shouldCallStoreInventoryBySKUMapperToMapStoreSkuInventoryForProductMessageToStoreInvenotryBySKUDocument() {
    SkuStoresInventoryMessage message = new SkuStoresInventoryMessageBuilder().build();

    storeSkuInventorySyncService.updateStoreSkuInventory(message);
    
    ArgumentCaptor<SkuStoresInventoryMessage> captor = ArgumentCaptor.forClass(SkuStoresInventoryMessage.class);
    verify(storeInventoryBySKUMapper).map(captor.capture());
    SkuStoresInventoryMessage actual = captor.getValue();
    assertThat("Mapper is called with expected message", actual, equalTo(message));
    
  }
  
  @Test
  public void shouldCallUpsertStoreInventoryBySKURepository() {
    
    SkuStoresInventoryMessage message = new SkuStoresInventoryMessageBuilder().build();
    StoreInventoryBySKUDocument document1 = new StoreInventoryBySKUDocument();
    document1.setSkuId("SKUID1");
    StoreInventoryItem invItem1 = new StoreInventoryItem();
    invItem1.setStoreNumber("STORE1");
    invItem1.setStoreId("STORE1/LOCATION1");
    invItem1.setLocationNumber("LOCATION1");
    invItem1.setQuantity(999);
    invItem1.setBopsQuantity(99);
    document1.setStoreInventoryItems(Collections.singletonList(invItem1));
    
    StoreInventoryBySKUDocument document2 = new StoreInventoryBySKUDocument();
    document2.setSkuId("SKUID");
    StoreInventoryItem invItem2 = new StoreInventoryItem();
    invItem2.setStoreNumber("STORE2");
    invItem2.setStoreId("STORE2/LOCATION2");
    invItem2.setLocationNumber("LOCATION2");
    invItem2.setQuantity(999);
    invItem2.setBopsQuantity(99);
    document2.setStoreInventoryItems(Collections.singletonList(invItem2));
    
    when(storeInventoryBySKUMapper.map(message)).thenReturn(Arrays.asList(document1, document2));
    
    when(storeInventoryBySKURepository.save(anyList())).thenReturn(Arrays.asList(document1, document2));
    
    boolean actual = storeSkuInventorySyncService.updateStoreSkuInventory(message);
    
    verify(storeInventoryBySKURepository).save(Arrays.asList(document1, document2));
    
    assertTrue("Upsert store sku invenotry method should return true", actual);
    
  }
  
}

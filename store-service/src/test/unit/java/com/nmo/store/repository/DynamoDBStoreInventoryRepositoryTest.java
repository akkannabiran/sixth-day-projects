package com.sixthday.store.repository;

import com.amazonaws.services.dynamodbv2.model.InternalServerErrorException;
import com.amazonaws.services.dynamodbv2.model.LimitExceededException;
import com.sixthday.store.data.StoreInventoryBySKUDocumentBuilder;
import com.sixthday.store.data.StoreInventoryItemBuilder;
import com.sixthday.store.models.StoreInventoryBySKUDocument;
import com.sixthday.store.models.StoreInventoryItem;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryDocument;
import com.sixthday.store.repository.dynamodb.DynamoDBStoreInventoryRepository;
import com.sixthday.store.repository.dynamodb.StoreInventoryBySKURepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DynamoDBStoreInventoryRepositoryTest {

  @Mock
  private StoreInventoryBySKURepository dynamoDB;

  @InjectMocks
  private DynamoDBStoreInventoryRepository repository;

  @Test
  public void shouldReturnEmptyStoreSkuInventoryDocumentsWhenDynamoDBDoesntHaveStoreInventory() {
    StoreInventoryBySKUDocumentBuilder dynamoDocBuilder = new StoreInventoryBySKUDocumentBuilder();
    List<StoreInventoryItem> expectedStoreInventories = null;
    StoreInventoryBySKUDocument dynamoDoc = dynamoDocBuilder.withSkuId("SkuId1").withStoreInventoryItems(expectedStoreInventories).build();
    when(dynamoDB.findOne(anyString())).thenReturn(dynamoDoc);

    List<StoreSkuInventoryDocument> storeSkus = repository.getAllStoresSkus(Arrays.asList("Store1", "Store2"), "SkuId1");

    verify(dynamoDB).findOne("SkuId1");
    assertTrue("StoreSkuInventoryDocument list is empty", storeSkus.isEmpty());
  }

  @Test
  public void shouldReturnEmptyStoreSkuInventoryDocumentsWhenDynamoDBDoesntHaveSku() {
    when(dynamoDB.findOne("SoldOutSku1")).thenReturn(null);
    List<StoreSkuInventoryDocument> storeSkus = repository.getAllStoresSkus(Arrays.asList("Store1", "Store2"), "SoldOutSku1");

    verify(dynamoDB).findOne("SoldOutSku1");
    assertTrue("StoreSkuInventoryDocument list is empty", storeSkus.isEmpty());
  }

  @Test
  public void shouldNotQueryDynamoDBIfInputStoreListIsNullOrEmpty() {

    List<StoreSkuInventoryDocument> storeSkus = repository.getAllStoresSkus(Collections.emptyList(), "AnySku");
    verify(dynamoDB, never()).findOne(anyString());
    assertTrue("StoreSkuInventoryDocument list is empty", storeSkus.isEmpty());

    storeSkus = repository.getAllStoresSkus(null, "AnySku");
    verify(dynamoDB, never()).findOne(anyString());
    assertTrue("StoreSkuInventoryDocument list is empty", storeSkus.isEmpty());

  }

  @Test
  public void shouldReturnStoreSkuInventoryDocumentsWhenDynamoDBHaveStoreInventories() {
    StoreInventoryBySKUDocumentBuilder dynamoDocBuilder = new StoreInventoryBySKUDocumentBuilder();
    StoreInventoryItemBuilder itemBuilder =  new StoreInventoryItemBuilder();
    List<StoreInventoryItem> expectedStoreInventories = Arrays.asList(
            itemBuilder.withAll("Store1", "Store1/Location1", "Location1", "1", 100, 50 ).build(),
            itemBuilder.withAll("Store2", "Store2/Location2", "Location2", "1", 200, 150 ).build());
    StoreInventoryBySKUDocument dynamoDoc = dynamoDocBuilder.withSkuId("SkuId1").withStoreInventoryItems(expectedStoreInventories).build();
    when(dynamoDB.findOne(anyString())).thenReturn(dynamoDoc);

    List<StoreSkuInventoryDocument> storeSkus = repository.getAllStoresSkus(Arrays.asList("Store1", "Store2", "StoreX", "StoreY", "StoreZ"), "SkuId1");
    verify(dynamoDB).findOne("SkuId1");

    assertThat("StoreSkuInventory document count should be two", storeSkus.size(), equalTo(2));

    assertStoreSkuInventoryDocument(storeSkus.get(0),"Store1", "Store1/Location1", "Location1", "1", 100, 50 );
    assertStoreSkuInventoryDocument(storeSkus.get(1),"Store2", "Store2/Location2", "Location2", "1", 200, 150);
  }
  
  @Test
  public void shouldNotReturnNullWhenDynamoDBDoesntHaveInventoryForGivenSku() {
	StoreInventoryBySKUDocumentBuilder dynamoDocBuilder = new StoreInventoryBySKUDocumentBuilder();
    StoreInventoryBySKUDocument dynamoDoc = dynamoDocBuilder.withSkuId("SkuId1").withStoreInventoryItems(Collections.emptyList()).build();
    when(dynamoDB.findOne(anyString())).thenReturn(dynamoDoc);

    List<StoreSkuInventoryDocument> storeSkus = repository.getAllStoresSkus(Arrays.asList("Store1", "Store2", "StoreX", "StoreY", "StoreZ"), "SkuId1");
    verify(dynamoDB).findOne("SkuId1");
    assertNotNull("StoreSku list should never be null from repository. Return empty list instead", storeSkus);
  }
  
  @Test
  public void shouldNotReturnNullWhenDynamoDBDoesntHaveSku() {
	when(dynamoDB.findOne(anyString())).thenReturn(null);
    List<StoreSkuInventoryDocument> storeSkus = repository.getAllStoresSkus(Arrays.asList("Store1", "Store2", "StoreX", "StoreY", "StoreZ"), "SkuId1");
    verify(dynamoDB).findOne("SkuId1");
    assertNotNull("StoreSku list should never be null from repository. Return empty list instead", storeSkus);
  }
  
  @Test(expected = LimitExceededException.class)
  public void shouldThrowLimitExceededExceptionWhenDynamoDBThrowsLimitExceededExceptionn() {
	when(dynamoDB.findOne(anyString())).thenThrow(new LimitExceededException("Expected exception from test case"));
    
	repository.getAllStoresSkus(Arrays.asList("Store1", "Store2", "StoreX", "StoreY", "StoreZ"), "SkuId1");
    
	verify(dynamoDB).findOne("SkuId1");
    
  }
  
  @Test(expected = InternalServerErrorException.class)
  public void shouldThrowInternalServerErrorExceptionWhenDynamoDBThrowsInternalServerErrorException() {
	when(dynamoDB.findOne(anyString())).thenThrow(new InternalServerErrorException("Expected exception from test case"));
    
	repository.getAllStoresSkus(Arrays.asList("Store1", "Store2", "StoreX", "StoreY", "StoreZ"), "SkuId1");
    
	verify(dynamoDB).findOne("SkuId1");
    
  }
  
  @Test(expected = Exception.class)
  public void shouldThrowExceptionWhenDynamoDBThrowsException() {
	when(dynamoDB.findOne(anyString())).thenThrow(new Exception("Expected exception from test case"));
    
	repository.getAllStoresSkus(Arrays.asList("Store1", "Store2", "StoreX", "StoreY", "StoreZ"), "SkuId1");
    
	verify(dynamoDB).findOne("SkuId1");
    
  }
  
  @Test
  public void shouldReturnStoreSkuInventoryDocumentsWhenDynamoDBHaveStoreInventoriesMatchingInputStores() {
    StoreInventoryBySKUDocumentBuilder dynamoDocBuilder = new StoreInventoryBySKUDocumentBuilder();
    StoreInventoryItemBuilder itemBuilder =  new StoreInventoryItemBuilder();
    List<StoreInventoryItem> expectedStoreInventories = Arrays.asList(
            itemBuilder.withAll("Store1", "Store1/Location1", "Location1", "1", 100, 50 ).build(),
            itemBuilder.withAll("Store2", "Store2/Location2", "Location2", "1", 200, 150 ).build(),
            itemBuilder.withAll("StoreA", "StoreA/LocationT", "LocationT", "1", 30, 9 ).build(),
            itemBuilder.withAll("StoreB", "StoreB/LocationD", "LocationD", "1", 100, 50 ).build(),
            itemBuilder.withAll("StoreC", "StoreC/LocationD", "LocationD", "1", 100, 50 ).build());
    StoreInventoryBySKUDocument dynamoDoc = dynamoDocBuilder.withSkuId("SkuId1").withStoreInventoryItems(expectedStoreInventories).build();
    when(dynamoDB.findOne(anyString())).thenReturn(dynamoDoc);

    List<StoreSkuInventoryDocument> storeSkus = repository.getAllStoresSkus(Arrays.asList("StoreA", "Store2", "Store1", "StoreY", "StoreZ"), "SkuId1");
    verify(dynamoDB).findOne("SkuId1");

    assertStoreSkuInventoryDocument(storeSkus.get(0),"Store1", "Store1/Location1", "Location1", "1", 100, 50 );
    assertStoreSkuInventoryDocument(storeSkus.get(1),"Store2", "Store2/Location2", "Location2", "1", 200, 150);
    assertStoreSkuInventoryDocument(storeSkus.get(2),"StoreA", "StoreA/LocationT", "LocationT", "1", 30, 9 );
  }

  @Test
  public void shouldReturnRequestedSKUsWhenItIsAvailable() {
    StoreInventoryBySKUDocumentBuilder dynamoDocBuilder = new StoreInventoryBySKUDocumentBuilder();
    StoreInventoryItemBuilder itemBuilder = new StoreInventoryItemBuilder();
    List<StoreInventoryItem> expectedStoreInventories = Arrays.asList(
            itemBuilder.withAll("Store1", "Store1/Location1", "Location1", "1", 100, 50).build(),
            itemBuilder.withAll("StoreC", "StoreC/LocationD", "LocationD", "1", 100, 50).build());
    StoreInventoryBySKUDocument storeInventoryBySKUDocument = dynamoDocBuilder.withSkuId("SkuId1").withStoreInventoryItems(expectedStoreInventories).build();

    when(dynamoDB.findAll(new HashSet<>(Collections.singletonList("SkuId1")))).thenReturn(Collections.singletonList(storeInventoryBySKUDocument));

    Iterable<StoreInventoryBySKUDocument> storeInventoryBySKUDocuments = repository.getSKUsInventory(new HashSet<>(Collections.singletonList("SkuId1")));

    assertNotNull(storeInventoryBySKUDocuments);
    assertThat(storeInventoryBySKUDocuments.iterator().hasNext(), is(true));
    assertThat(storeInventoryBySKUDocuments.iterator().next().getSkuId(), is("SkuId1"));
  }

  @Test
  public void shouldReturnEmptyListWhenItIsNotAvailable() {
    when(dynamoDB.findAll(Collections.singletonList("SkuId1"))).thenReturn(null);

    Iterable<StoreInventoryBySKUDocument> storeInventoryBySKUDocuments = repository.getSKUsInventory(new HashSet<>(Collections.singletonList("SkuId1")));

    assertNotNull(storeInventoryBySKUDocuments);
    assertThat(storeInventoryBySKUDocuments.iterator().hasNext(), is(false));
  }

  private void assertStoreSkuInventoryDocument(StoreSkuInventoryDocument storeSkuInventoryDocument, String storeNum, String storeId, String locationNum, String invLevel, int qty, int bopsQty) {
    assertThat("Store number should be equal to one in dynamodb", storeSkuInventoryDocument.getStoreNumber(), equalTo(storeNum));
    assertThat("Store id should be equal to one in dynamodb", storeSkuInventoryDocument.getStoreId(), equalTo(storeId));
    assertThat("Location number should be equal to one in dynamodb", storeSkuInventoryDocument.getLocationNumber(), equalTo(locationNum));
    assertThat("Inventory level should be equal to one in dynamodb", storeSkuInventoryDocument.getInventoryLevelCode(), equalTo(invLevel));
    assertThat("Quantity  should be equal to one in dynamodb", storeSkuInventoryDocument.getQuantity(), equalTo(qty));
    assertThat("Bops quantity should be equal to one in dynamodb", storeSkuInventoryDocument.getBopsQuantity(), equalTo(bopsQty));
  }

}

package com.sixthday.store.models;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@DynamoDBTable(tableName = "nm_store_inventory_by_sku")
public class StoreInventoryBySKUDocument {
  @DynamoDBHashKey(attributeName = "sku_id")
  private String skuId;
  
  private List<StoreInventoryItem> storeInventoryItems = new ArrayList<>();
}

package com.sixthday.store.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@DynamoDBDocument
@Setter
@Getter
@EqualsAndHashCode
@ToString
public class StoreInventoryItem {
  @DynamoDBRangeKey(attributeName = "store_number")
  private String storeNumber;
  @DynamoDBAttribute(attributeName = "store_id")
  private String storeId;
  @DynamoDBAttribute(attributeName = "location_number")
  private String locationNumber;
  @DynamoDBAttribute(attributeName = "inventory_level")
  private String inventoryLevel;
  @DynamoDBAttribute(attributeName = "quantity")
  private int quantity;
  @DynamoDBAttribute(attributeName = "bops_quantity")
  private int bopsQuantity;
}
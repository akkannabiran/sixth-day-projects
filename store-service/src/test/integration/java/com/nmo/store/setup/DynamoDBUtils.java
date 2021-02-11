package com.sixthday.store.setup;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.sixthday.store.models.StoreInventoryBySKUDocument;

import lombok.SneakyThrows;

public class DynamoDBUtils {
  private DynamoDBUtils() {
  }

  @SneakyThrows
  public static StoreInventoryBySKUDocument getStoresInventoryItem(final String skuId, final String storeNumber, final DynamoDBMapperConfig mapperConfig, final AmazonDynamoDB amazonDynamoDB) {
    DynamoDBMapper mapper = new DynamoDBMapper(amazonDynamoDB);
    return mapper.load(StoreInventoryBySKUDocument.class, skuId, storeNumber, mapperConfig);
  }

  public static List<StoreInventoryBySKUDocument> getStoresInventoryBySkuId(final String skuId, final DynamoDBMapperConfig mapperConfig, final AmazonDynamoDB amazonDynamoDB) {
    DynamoDBMapper mapper = new DynamoDBMapper(amazonDynamoDB);
    DynamoDBQueryExpression<StoreInventoryBySKUDocument> queryExpression =
            new DynamoDBQueryExpression<StoreInventoryBySKUDocument>().withKeyConditionExpression("#sku_id = :sku_id").withExpressionAttributeNames(Collections.singletonMap("#sku_id", "sku_id"))
                    .withExpressionAttributeValues(Collections.singletonMap(":sku_id", new AttributeValue().withS(skuId)));
    return  mapper.query(StoreInventoryBySKUDocument.class, queryExpression, mapperConfig).stream().collect(Collectors.toList());
  }

}

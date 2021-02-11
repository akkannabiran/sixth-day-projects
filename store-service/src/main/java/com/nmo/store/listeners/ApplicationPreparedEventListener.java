package com.sixthday.store.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.amazonaws.services.dynamodbv2.util.TableUtils.TableNeverTransitionedToStateException;
import com.sixthday.store.config.AwsDynamoDbConfig;
import com.sixthday.store.models.StoreInventoryBySKUDocument;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ApplicationPreparedEventListener implements ApplicationListener<ApplicationPreparedEvent> {
  @Autowired
  private AwsDynamoDbConfig awsDynamoDbConfig;
  @Autowired
  private AmazonDynamoDB amazonDynamoDB;
  @Autowired
  private DynamoDBMapperConfig dynamoDBMapperConfig;
  
  @Override
  public void onApplicationEvent(ApplicationPreparedEvent event) {
    String tableName = "";
    try {
      tableName = dynamoDBMapperConfig.getTableNameResolver().getTableName(StoreInventoryBySKUDocument.class, dynamoDBMapperConfig);
      // Check if requried <<env>>_nm_store_inventory_by_sku table exists.
      createStoreInventoryBySkuTableIfNotExits(tableName);
      log.info("Application start up complete. EventType=\"ApplicationStart\", EnvironmentSpecificTableName=\"{}\", Status=\"Success\". ", tableName);
    } catch (Exception e) {
      log.error("Application start up aborted. EventType=\"ApplicationStart\", EnvironmentSpecificTableName=\"{}\", Status=\"Failed\", Error=\"{}\"", tableName, e.getMessage(), e);
      throw e;
    }
  }
  
  @SneakyThrows
  private boolean createStoreInventoryBySkuTableIfNotExits(String tableName) {
    boolean status =  false;
    ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput()
                                          .withReadCapacityUnits(awsDynamoDbConfig.getReadCapacityUnits())
                                          .withWriteCapacityUnits(awsDynamoDbConfig.getWriteCapacityUnits());
    
    CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName);
    createTableRequest.withKeySchema(new KeySchemaElement().withAttributeName("sku_id").withKeyType(KeyType.HASH));
    createTableRequest.withAttributeDefinitions(new AttributeDefinition().withAttributeName("sku_id").withAttributeType(ScalarAttributeType.S));
    createTableRequest.setProvisionedThroughput(provisionedThroughput);
    boolean isCreated = TableUtils.createTableIfNotExists(amazonDynamoDB, createTableRequest);
    if (isCreated) {
      try {
        TableUtils.waitUntilActive(amazonDynamoDB, tableName);
        status = true;
      } catch (TableNeverTransitionedToStateException | InterruptedException e) {
        throw e;
      }
    } else {
      status = true;
    }
    return status;
  }

}

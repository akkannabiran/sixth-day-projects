package com.sixthday.store.repository.dynamodb;

import com.sixthday.logger.logging.Loggable;
import com.sixthday.logger.logging.LoggableEvent;
import com.sixthday.store.config.StoreDetailsConfig;
import com.sixthday.store.models.StoreInventoryBySKUDocument;
import com.sixthday.store.models.StoreInventoryItem;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.sixthday.store.config.Constants.Actions.STORE_SEARCH_DYNAMO_DB;
import static com.sixthday.store.config.Constants.Events.REPOSITORY_EVENT;

@Repository("StoreInventoryDynamoDBRepository")
@Loggable
public class DynamoDBStoreInventoryRepository {

    private StoreInventoryBySKURepository storeInventoryBySKURepository;

    @Autowired
    public DynamoDBStoreInventoryRepository(final StoreDetailsConfig config, final StoreInventoryBySKURepository storeInventoryBySKURepository) {
        this.storeInventoryBySKURepository = storeInventoryBySKURepository;
    }

    @LoggableEvent(eventType = REPOSITORY_EVENT, action = STORE_SEARCH_DYNAMO_DB)
    public List<StoreSkuInventoryDocument> getAllStoresSkus(List<String> storeNumbers, String skuId) {
      if (!CollectionUtils.isEmpty(storeNumbers)) {
        StoreInventoryBySKUDocument dynamoSkuDocument = Optional.ofNullable(storeInventoryBySKURepository.findOne(skuId)).orElse(new StoreInventoryBySKUDocument());
        List<StoreInventoryItem> inventoryItems = Optional.ofNullable(dynamoSkuDocument.getStoreInventoryItems()).orElse(new ArrayList<>());
        return inventoryItems.stream().filter(Objects::nonNull).filter(item -> storeNumbers.contains(item.getStoreNumber())).map(this::mapToStoreSkuInventoryDocument).map(doc -> {
          doc.setSkuId(dynamoSkuDocument.getSkuId());
          return doc;
        }).collect(Collectors.toList());
      } else {
        return Collections.emptyList();
      }
    }

    @LoggableEvent(eventType = REPOSITORY_EVENT, action = STORE_SEARCH_DYNAMO_DB)
    public Iterable<StoreInventoryBySKUDocument> getSKUsInventory(Set<String> skuIds) {
        return Optional.ofNullable(storeInventoryBySKURepository.findAll(skuIds)).orElse(new ArrayList<>());
    }

    private StoreSkuInventoryDocument mapToStoreSkuInventoryDocument(StoreInventoryItem inventoryItem) {
      StoreSkuInventoryDocument document = new StoreSkuInventoryDocument();
      document.setStoreId(inventoryItem.getStoreId());
      document.setStoreNumber(inventoryItem.getStoreNumber());
      document.setLocationNumber(inventoryItem.getLocationNumber());
      document.setInventoryLevelCode(inventoryItem.getInventoryLevel());
      document.setBopsQuantity(inventoryItem.getBopsQuantity());
      document.setQuantity(inventoryItem.getQuantity());
      return document;
    }
}

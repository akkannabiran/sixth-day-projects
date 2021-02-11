package com.sixthday.store.services;

import static com.sixthday.store.config.Constants.Logging.CONTENT_SYNC_LOG_MAKER;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sixthday.store.StoreInventoryBySKUMapper;
import com.sixthday.store.exceptions.StoreSkuInventorySycFailedException;
import com.sixthday.store.models.StoreInventoryBySKUDocument;
import com.sixthday.store.models.storeinventoryindex.SkuStoresInventoryMessage;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryDocument;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryMessage;
import com.sixthday.store.repository.StoreSkuInventorySyncRepository;
import com.sixthday.store.repository.dynamodb.StoreInventoryBySKURepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StoreSkuInventorySyncService {

	private StoreSkuInventorySyncRepository storeSkuInventorySyncRepository;
	private StoreInventoryBySKUMapper storeInventoryBySKUMapper;
	private StoreInventoryBySKURepository storeInventoryBySKURepository;
	public static final String SUCCESS_LOG_FORMAT = "sixthdayLogType=\"ContentSyncDashboard\", OperationType=\"DYNDB_UPDATE\", Status=\"Success\", DurationInMs=\"{}\"";
	public static final String FAILURE_LOG_FORMAT = "sixthdayLogType=\"ContentSyncDashboard\", OperationType=\"DYNDB_UPDATE\", Status=\"Failed\",  DurationInMs=\"{}\", Error=\"{}\"";

	@Autowired
	public StoreSkuInventorySyncService(final StoreSkuInventorySyncRepository storeSkuInventorySyncRepository, final StoreInventoryBySKUMapper storeInventoryBySKUMapper, final StoreInventoryBySKURepository storeInventoryBySKURepository) {
		this.storeSkuInventorySyncRepository = storeSkuInventorySyncRepository;
		this.storeInventoryBySKUMapper = storeInventoryBySKUMapper;
		this.storeInventoryBySKURepository = storeInventoryBySKURepository;
	}

	public void updateStoreSkuInventory(StoreSkuInventoryDocument storeSkuInventoryDocument,
			StoreSkuInventoryMessage storeSkuInventoryMessage) throws StoreSkuInventorySycFailedException {
		if (storeSkuInventoryMessage.hasStoreSkuInventoryUpsertEventType()) {
			handleStoreSkuInventoryUpsert(storeSkuInventoryDocument);
		} else if (storeSkuInventoryMessage.hasStoreSkuInventoryRemovedEventType()) {
			handleStoreSkuInventoryDelete(storeSkuInventoryDocument);
		}
	}
  
  public boolean updateStoreSkuInventory(SkuStoresInventoryMessage skuStoresInventoryMessage) {
	Instant start = Instant.now();
	Iterable<StoreInventoryBySKUDocument> savedItems = null;
	try {
		List<StoreInventoryBySKUDocument> storeInventoryBySkuDocuments = storeInventoryBySKUMapper.map(skuStoresInventoryMessage);
		savedItems = storeInventoryBySKURepository.save(storeInventoryBySkuDocuments);
		log.info(CONTENT_SYNC_LOG_MAKER, SUCCESS_LOG_FORMAT, Duration.between(start, Instant.now()).toMillis());
	} catch (Exception e) {
		log.error(CONTENT_SYNC_LOG_MAKER, FAILURE_LOG_FORMAT, Duration.between(start, Instant.now()).toMillis(), e.getMessage());
		throw e;
	}
    return Objects.nonNull(savedItems);
  }
  
	private void handleStoreSkuInventoryUpsert(final StoreSkuInventoryDocument storeSkuInventoryDocument)
			throws StoreSkuInventorySycFailedException {
		storeSkuInventorySyncRepository.createOrUpdateStoreSkuInventory(storeSkuInventoryDocument);
	}

	private void handleStoreSkuInventoryDelete(final StoreSkuInventoryDocument storeSkuInventoryDocument)
			throws StoreSkuInventorySycFailedException {
		storeSkuInventorySyncRepository.deleteStoreSkuInventory(storeSkuInventoryDocument);
	}
}

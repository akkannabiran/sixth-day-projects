package com.sixthday.store.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.store.config.SubscriberConfiguration;
import com.sixthday.store.exceptions.StoreSkuInventorySycFailedException;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryDocument;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

import static com.sixthday.store.config.Constants.Logging.CONTENT_SYNC_LOG_MAKER;
import static com.sixthday.store.util.ElasticsearchActionRequestUtils.FAILURE_LOG_FORMAT;
import static com.sixthday.store.util.ElasticsearchActionRequestUtils.SUCCESS_LOG_FORMAT;

@Slf4j
@Component
public class StoreSkuInventorySyncRepository {
  private final RestHighLevelClient client;
  private ObjectMapper genericMapper;
  
  @Autowired
  SubscriberConfiguration subscriberConfiguration;
  
  @Autowired
  public StoreSkuInventorySyncRepository(SubscriberConfiguration subscriberConfiguration, RestHighLevelClient restHighLevelClient) {
    this.subscriberConfiguration = subscriberConfiguration;
    this.client = restHighLevelClient;
    this.genericMapper = new ObjectMapper();
  }
  
  public void createOrUpdateStoreSkuInventory(StoreSkuInventoryDocument storeSkuInventoryDocument) {
    log.debug("StoreSkuInventorySyncRepository=\"CREATE_OR_UPDATE_STORE_SKU_INVENTORY\", Status=\"Begin\", MessageId=\"{}\"", storeSkuInventoryDocument.getId());
    Instant start = Instant.now();
    try {
      IndexRequest indexRequest = createIndexRequest(storeSkuInventoryDocument, genericMapper.writeValueAsString(storeSkuInventoryDocument));
      UpdateRequest builder = createUpdateRequestBuilder(storeSkuInventoryDocument, indexRequest);
      executeAsyncUpdateRequest(builder, storeSkuInventoryDocument);
    } catch (JsonProcessingException e) {
      log.error("Error occurred while constructing the store update query", e);
    } finally {
      log.debug("StoreSyncRepositoryUpdate=\"CREATE_OR_UPDATE_STORE_REPOSITORY\", Status=\"End\", MessageId=\"{}\", DurationInMs=\"{}\"", storeSkuInventoryDocument.getId(),
              Duration.between(start, Instant.now()).toMillis());
    }
  }
  
  private UpdateRequest createUpdateRequestBuilder(StoreSkuInventoryDocument storeSkuInventoryDocument, IndexRequest indexRequest) throws JsonProcessingException {
    return new UpdateRequest(subscriberConfiguration.getElasticSearchConfig().getStoreSkuInventoryIndexName(), StoreSkuInventoryDocument.DOCUMENT_TYPE, storeSkuInventoryDocument.getId()).
            doc(genericMapper.writeValueAsString(storeSkuInventoryDocument),XContentType.JSON).upsert(indexRequest).docAsUpsert(true);
  }
  
  public IndexRequest createIndexRequest(StoreSkuInventoryDocument storeSkuInventoryDocument, String document) {
    return new IndexRequest(subscriberConfiguration.getElasticSearchConfig().getStoreSkuInventoryIndexName(),
            StoreSkuInventoryDocument.DOCUMENT_TYPE,
            storeSkuInventoryDocument.getId()).
            source(document, XContentType.JSON);
  }
  
  protected void executeAsyncUpdateRequest(UpdateRequest updateRequest, final StoreSkuInventoryDocument storeSkuInventoryDocument) {
    client.updateAsync(updateRequest, RequestOptions.DEFAULT, new ESUpdateActionListener(Instant.now(), MDC.getCopyOfContextMap()));
  }
  
  public void deleteStoreSkuInventory(StoreSkuInventoryDocument storeSkuInventoryDocument) throws StoreSkuInventorySycFailedException {
    log.debug("StoreSkuInventorySyncRepository=\"DELETE_STORE_SKU_INVENTORY\", Status=\"Begin\", MessageId=\"{}\"", storeSkuInventoryDocument.getId());
    Instant start = Instant.now();
    DeleteRequest deleteRequest = new DeleteRequest(subscriberConfiguration.getElasticSearchConfig().getStoreSkuInventoryIndexName(),StoreSkuInventoryDocument.DOCUMENT_TYPE,storeSkuInventoryDocument.getId());
    client.deleteAsync(deleteRequest, RequestOptions.DEFAULT, new ActionListener<DeleteResponse>() {
      @Override
      public void onResponse(DeleteResponse deleteResponse) {
        log.info(CONTENT_SYNC_LOG_MAKER, SUCCESS_LOG_FORMAT, Duration.between(start, Instant.now()).toMillis());
        log.debug("StoreSkuInventorySyncRepository=\"DELETE_STORE_SKU_INVENTORY\", Status=\"End\", MessageId=\"{}\", DurationInMs=\"{}\"", storeSkuInventoryDocument.getId(),
                Duration.between(start, Instant.now()).toMillis());

      }
      @Override
      public void onFailure(Exception e) {
        log.error(CONTENT_SYNC_LOG_MAKER, FAILURE_LOG_FORMAT, Duration.between(start, Instant.now()).toMillis(), "Could not clear store sku inventory on elastic search index.");
      }
    });
  }

}

package com.sixthday.store.data;

import static com.sixthday.store.models.storeinventoryindex.SkuStoresInventoryMessage.EventType.SKU_STORES_INVENTORY_UPDATED;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.store.models.storeinventoryindex.SkuStore;
import com.sixthday.store.models.storeinventoryindex.SkuStoresInventoryMessage.EventType;
import com.sixthday.store.models.storeinventoryindex.SkuStoresInventoryMessage;
import lombok.SneakyThrows;;

public class SkuStoresInventoryMessageBuilder {
  @JsonProperty("id")
  private String productId = "PRODUCT-ID";
  @JsonProperty("skuStores")
  private List<SkuStore> skuStores = Collections.singletonList(new SkuStore());
  private EventType eventType = SKU_STORES_INVENTORY_UPDATED;
  private String batchId = "BATCH-ID";
  private Map<String, String> originTimestampInfo = Collections.singletonMap("SKU_STORES_INVENTORY_UPDATED", "SomeOriginTimestamp");
  private Map<String, Object> dataPoints = Collections.singletonMap("somekey", "someValue");
  
  public SkuStoresInventoryMessage build() {
    SkuStoresInventoryMessage msg = new SkuStoresInventoryMessage();
    msg.setBatchId(batchId);
    msg.setEventType(eventType);
    msg.setProductId(productId);
    msg.setSkuStores(skuStores);
    msg.setOriginTimestampInfo(originTimestampInfo);
    msg.setDataPoints(dataPoints);
    return msg;
  }
  
  @SneakyThrows
  public String toJson() {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(this.build());
  }
  
  public SkuStoresInventoryMessageBuilder withProductId(String productId) {
    this.productId = productId;
    return this;
  }
  
  public SkuStoresInventoryMessageBuilder withBatchId(String batchId) {
    this.batchId = batchId;
    return this;
  }
  
  public SkuStoresInventoryMessageBuilder withEventType(EventType eventType) {
    this.eventType = eventType;
    return this;
  }
  
  public SkuStoresInventoryMessageBuilder withSkuStores(List<SkuStore> skuStores) {
    this.skuStores = skuStores;
    return this;
  }
  
  public SkuStoresInventoryMessageBuilder withOriginTimestampInfo(Map<String, String> originTimestampInfo) {
    this.originTimestampInfo = originTimestampInfo;
    return this;
  }
  
  public SkuStoresInventoryMessageBuilder withDataPoints(Map<String, Object> dataPoints) {
    this.dataPoints = dataPoints;
    return this;
  }
  
}

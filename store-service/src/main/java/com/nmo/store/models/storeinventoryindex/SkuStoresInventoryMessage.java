package com.sixthday.store.models.storeinventoryindex;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown=true)
public class SkuStoresInventoryMessage {
  public  enum EventType {
    SKU_STORES_INVENTORY_UPDATED
  }
  @JsonProperty("id")
  private String productId;
  private List<SkuStore> skuStores;
  private EventType eventType;
  private String batchId;
  private Map<String, String> originTimestampInfo;
  private Map<String, Object> dataPoints;
}

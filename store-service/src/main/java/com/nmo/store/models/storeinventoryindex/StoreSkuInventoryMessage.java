package com.sixthday.store.models.storeinventoryindex;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StoreSkuInventoryMessage extends StoreSkuInventoryDocument {
  private EventType eventType;
  @JsonProperty(access = Access.WRITE_ONLY)
  private String batchId;
  @JsonProperty(access = Access.WRITE_ONLY)
  private Map<String, String> originTimestampInfo;
  @JsonProperty(access = Access.WRITE_ONLY)
  private Map<String, Object> dataPoints;
  
  public enum EventType {
    STORE_SKU_INVENTORY_UPSERT, STORE_SKU_INVENTORY_DELETE
  }
  
  @JsonIgnore
  public boolean isValid() {
    return eventType != null;
  }
  
  @JsonIgnore
  public boolean isInvalid() {
    return !isValid();
  }
  
  @JsonIgnore
  public boolean hasStoreSkuInventoryUpsertEventType() {
    return eventType == EventType.STORE_SKU_INVENTORY_UPSERT;
  }
  
  @JsonIgnore
  public boolean hasStoreSkuInventoryRemovedEventType() {
    return eventType == EventType.STORE_SKU_INVENTORY_DELETE;
  }
  
}

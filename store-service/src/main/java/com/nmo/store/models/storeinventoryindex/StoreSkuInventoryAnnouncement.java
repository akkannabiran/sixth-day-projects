package com.sixthday.store.models.storeinventoryindex;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StoreSkuInventoryAnnouncement {
  private List<String> productIds;
  private String skuId;
  private String storeNumber;
  private String inventoryLevel;
  private int quantity;
  private boolean removed;
  private String batchId;
  private Map<String, String> originTimestampInfo;
  private Map<String, Object> dataPoints;
}

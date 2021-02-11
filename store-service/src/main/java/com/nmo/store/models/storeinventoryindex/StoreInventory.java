package com.sixthday.store.models.storeinventoryindex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StoreInventory {
  @JsonProperty("storeNo")
  private String storeNumber;
  private String storeId;
  private String locationNumber;
  private String invLevel;
  @JsonProperty("qty")
  private int quantity;
  @JsonProperty("bopsQty")
  private int bopsQuantity;
}

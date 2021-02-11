package com.sixthday.store.models.storeinventoryindex;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
public class StoreSkuInventoryDocument {
	public static final String MSG_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String DOCUMENT_TYPE = "_doc";
	private String id;
	private String skuId;
	private String storeNumber;
	private String storeId;
	private String locationNumber;
	private String inventoryLevelCode;
	private int quantity;
	private int bopsQuantity;
	private List<String> productIds;
}

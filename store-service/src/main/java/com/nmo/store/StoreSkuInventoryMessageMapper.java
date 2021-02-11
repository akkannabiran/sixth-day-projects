package com.sixthday.store;

import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryDocument;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryMessage;

public class StoreSkuInventoryMessageMapper {
	public StoreSkuInventoryDocument map(StoreSkuInventoryMessage storeSkuInventoryMessage) {
		StoreSkuInventoryDocument storeSkuInventoryDocument = new StoreSkuInventoryDocument();
		storeSkuInventoryDocument.setId(storeSkuInventoryMessage.getId());
		storeSkuInventoryDocument.setSkuId(storeSkuInventoryMessage.getSkuId());
		storeSkuInventoryDocument.setStoreNumber(storeSkuInventoryMessage.getStoreNumber());
		storeSkuInventoryDocument.setStoreId(storeSkuInventoryMessage.getStoreId());
		storeSkuInventoryDocument.setLocationNumber(storeSkuInventoryMessage.getLocationNumber());
		storeSkuInventoryDocument.setInventoryLevelCode(storeSkuInventoryMessage.getInventoryLevelCode());
		storeSkuInventoryDocument.setQuantity(storeSkuInventoryMessage.getQuantity());
		storeSkuInventoryDocument.setBopsQuantity(storeSkuInventoryMessage.getBopsQuantity());
		storeSkuInventoryDocument.setProductIds(storeSkuInventoryMessage.getProductIds());
		return storeSkuInventoryDocument;
	}
}
package com.sixthday.store.exceptions;

import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryDocument;

import lombok.Getter;

public class StoreSkuInventorySycFailedException extends Exception {

	private static final long serialVersionUID = -1053215183596491267L;
	@Getter
	private final transient StoreSkuInventoryDocument storeSkuInventoryDocument;

	public StoreSkuInventorySycFailedException(final StoreSkuInventoryDocument storeSkuInventoryDocument,
			final String message) {
		super(message);
		this.storeSkuInventoryDocument = storeSkuInventoryDocument;
	}
}

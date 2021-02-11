package com.sixthday.store.exceptions;

import com.sixthday.store.models.storeindex.StoreDocument;

import lombok.Getter;

public class StoreUpsertFailedException extends Exception {

	private static final long serialVersionUID = 740198962796892239L;
	@Getter
	private final transient StoreDocument storeDocument;

	public StoreUpsertFailedException(final StoreDocument storeDocument, final String message) {
		super(message);
		this.storeDocument = storeDocument;
	}
}

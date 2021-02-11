package com.sixthday.store.exceptions;

public class DocumentRetrievalException extends RuntimeException {
	private static final long serialVersionUID = -5989324292315236848L;

	public DocumentRetrievalException(Exception e) {
		super(e);
	}
}

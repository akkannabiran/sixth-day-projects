package com.sixthday.store.exceptions;

import java.io.IOException;

public class GotWWWResponseParseException extends RuntimeException {
	private static final long serialVersionUID = -7415919764603447952L;

	public GotWWWResponseParseException(IOException cause) {
		super(cause);
	}
}

package com.sixthday.store.exceptions;

public class InvalidLatitudeLongitudeException extends RuntimeException {
	private static final long serialVersionUID = 1520857425903391807L;

	public InvalidLatitudeLongitudeException(String message) {
		super(message);
	}
}

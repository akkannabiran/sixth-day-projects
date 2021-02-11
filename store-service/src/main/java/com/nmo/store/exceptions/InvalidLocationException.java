package com.sixthday.store.exceptions;

public class InvalidLocationException extends RuntimeException {
	private static final long serialVersionUID = 4192789844313144173L;

	public InvalidLocationException(String message) {
		super(String.format("Invalid location: %s", message));
	}
}

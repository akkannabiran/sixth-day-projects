package com.sixthday.navigation.exceptions;

public class NavigationServiceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NavigationServiceException(final String message) {
        super(message);
    }

    public NavigationServiceException(final String message, Throwable cause) {
        super(message, cause);
    }
}

package com.sixthday.navigation.exceptions;

public class NavigationBatchServiceException extends RuntimeException {
    private static final long serialVersionUID = 6326162815284368647L;

    public NavigationBatchServiceException(final String message) {
        super(message);
    }

    public NavigationBatchServiceException(final String message, Throwable cause) {
        super(message, cause);
    }
}

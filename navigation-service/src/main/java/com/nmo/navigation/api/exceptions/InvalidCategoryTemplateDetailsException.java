package com.sixthday.navigation.api.exceptions;

public class InvalidCategoryTemplateDetailsException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidCategoryTemplateDetailsException(String message) {
        super(message);
    }
}

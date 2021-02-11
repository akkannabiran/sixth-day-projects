package com.sixthday.navigation.api.exceptions;

public class CategoryNotFoundException extends RuntimeException {
    public static final String ERROR_MESSAGE = "Category information is not available for the requested category ";
    private static final long serialVersionUID = 1L;

    public CategoryNotFoundException(String categoryId) {
        super(ERROR_MESSAGE + categoryId);
    }
}

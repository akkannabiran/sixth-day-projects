package com.sixthday.category.exceptions;

public class CategoryDocumentNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private static final String ERROR_MESSAGE = "Unable to get the categories for the requested Id ";

    public CategoryDocumentNotFoundException(String categoryId) {
        super(ERROR_MESSAGE + categoryId);
    }
}

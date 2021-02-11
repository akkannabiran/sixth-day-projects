package com.sixthday.category.exceptions;

public class UnknownCategoryTemplateTypeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UnknownCategoryTemplateTypeException(String categoryId, String templateType) {
        super("Found unknown template type " + templateType + " for the requested Id " + categoryId);
    }
}

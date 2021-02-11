package com.sixthday.navigation.integration.exceptions;

import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import lombok.Getter;

@Getter
public class CategoryMessageProcessingException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final transient CategoryDocument categoryDocument;

    public CategoryMessageProcessingException(final CategoryDocument categoryDocument, final String message) {
        super(message);
        this.categoryDocument = categoryDocument;
    }
}

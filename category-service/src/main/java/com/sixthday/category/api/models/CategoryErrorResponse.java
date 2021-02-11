package com.sixthday.category.api.models;

import lombok.Getter;

@Getter
public class CategoryErrorResponse {
    private String message;
    private int statusCode;

    public CategoryErrorResponse(final String message, final int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }
}

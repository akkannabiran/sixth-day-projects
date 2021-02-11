package com.sixthday.navigation.api.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NavigationErrorResponse {
    private String message;
    private int statusCode;

    public NavigationErrorResponse(final String message, final int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }
}

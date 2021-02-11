package com.sixthday.navigation.api.exceptions;

public class BreadcrumbNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public BreadcrumbNotFoundException(String categoryId) {
        super("Breadcrumb information is not available because category " + categoryId + " is not found.");
    }

    public BreadcrumbNotFoundException(String categoryId, Throwable e) {
        super("Breadcrumb information is not available because category " + categoryId + " is not found.", e);
    }

}

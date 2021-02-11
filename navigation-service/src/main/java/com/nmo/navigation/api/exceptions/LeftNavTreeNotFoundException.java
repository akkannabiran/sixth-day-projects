package com.sixthday.navigation.api.exceptions;

public class LeftNavTreeNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public LeftNavTreeNotFoundException(String categoryId) {
        super(
                "Left nav tree information is not available for the requested category ["
                        + categoryId
                        + "]. "
        );
    }

    public LeftNavTreeNotFoundException(String categoryId, Throwable e) {
        super(
                "Left nav tree information is not available for the requested category ["
                        + categoryId
                        + "]. "
                , e);
    }
}

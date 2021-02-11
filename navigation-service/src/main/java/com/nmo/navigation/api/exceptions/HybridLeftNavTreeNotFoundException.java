package com.sixthday.navigation.api.exceptions;

public class HybridLeftNavTreeNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public HybridLeftNavTreeNotFoundException(String categoryId) {
        super(
                "Left nav tree information is not available for the requested category ["
                        + categoryId
                        + "]. "
        );
    }

    public HybridLeftNavTreeNotFoundException(String categoryId, Throwable e) {
        super(
                "Left nav tree information is not available for the requested category ["
                        + categoryId
                        + "]. "
                , e);
    }
}

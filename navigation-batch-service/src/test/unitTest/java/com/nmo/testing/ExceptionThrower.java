package com.sixthday.testing;

public interface ExceptionThrower {
    static Throwable assertThrown(ExceptionThrower exceptionThrower) {
        try {
            exceptionThrower.throwException();
        } catch (Throwable caught) {
            return caught;
        }
        throw new RuntimeException("Expected an exception to be thrown");
    }

    void throwException() throws Throwable;
}

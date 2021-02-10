package com.sixthday.kafka.consumer.error.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UnrecoverableMessageExceptionTest {

    @Test
    public void testRecoverableMessageExceptionConstructorWithTwoParameters() {
        RuntimeException runtimeException = new RuntimeException("some-root-cause");
        UnrecoverableMessageException unrecoverableMessageException = new UnrecoverableMessageException("some-error", runtimeException);

        assertTrue(unrecoverableMessageException.isPublishMessageToTopic());
        assertEquals(runtimeException, unrecoverableMessageException.getActualCause());
        assertEquals(runtimeException.getMessage(), unrecoverableMessageException.getActualCause().getMessage());
    }

    @Test
    public void testRecoverableMessageExceptionConstructorWithThreeParameters() {
        RuntimeException runtimeException = new RuntimeException("some-root-cause");
        UnrecoverableMessageException unrecoverableMessageException = new UnrecoverableMessageException("some-error", runtimeException, true);

        assertTrue(unrecoverableMessageException.isPublishMessageToTopic());
        assertEquals(runtimeException, unrecoverableMessageException.getActualCause());
        assertEquals(runtimeException.getMessage(), unrecoverableMessageException.getActualCause().getMessage());
    }
}
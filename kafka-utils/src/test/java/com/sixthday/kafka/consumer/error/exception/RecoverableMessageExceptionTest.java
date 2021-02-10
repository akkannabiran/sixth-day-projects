package com.sixthday.kafka.consumer.error.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RecoverableMessageExceptionTest {

    @Test
    public void testRecoverableMessageExceptionConstructorWithTwoParameters() {
        RuntimeException runtimeException = new RuntimeException("some-root-cause");
        RecoverableMessageException recoverableMessageException = new RecoverableMessageException("some-error", runtimeException);

        assertTrue(recoverableMessageException.isPublishMessageToTopic());
        assertEquals(runtimeException, recoverableMessageException.getActualCause());
        assertEquals(runtimeException.getMessage(), recoverableMessageException.getActualCause().getMessage());
    }

    @Test
    public void testRecoverableMessageExceptionConstructorWithThreeParameters() {
        RuntimeException runtimeException = new RuntimeException("some-root-cause");
        RecoverableMessageException recoverableMessageException = new RecoverableMessageException("some-error", runtimeException, true);

        assertTrue(recoverableMessageException.isPublishMessageToTopic());
        assertEquals(runtimeException, recoverableMessageException.getActualCause());
        assertEquals(runtimeException.getMessage(), recoverableMessageException.getActualCause().getMessage());
    }
}
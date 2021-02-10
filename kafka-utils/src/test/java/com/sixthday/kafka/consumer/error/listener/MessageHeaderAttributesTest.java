package com.sixthday.kafka.consumer.error.listener;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageHeaderAttributesTest {

    @Test
    public void testStaticValues() {
        assertEquals("retry_count", MessageHeaderAttributes.RETRY_COUNT);
        assertEquals("topic_name", MessageHeaderAttributes.TOPIC_NAME);
        assertEquals("exception_msg", MessageHeaderAttributes.EXCEPTION_MESSAGE);
        assertEquals("exception_class", MessageHeaderAttributes.EXCEPTION_CLASS);
        assertEquals("root_cause_exception_msg", MessageHeaderAttributes.ROOT_CAUSE_EXCEPTION_MESSAGE);
        assertEquals("root_cause_exception_class", MessageHeaderAttributes.ROOT_CAUSE_EXCEPTION_CLASS);
        assertEquals("history", MessageHeaderAttributes.MESSAGE_HISTORY);
        assertEquals("timestamp", MessageHeaderAttributes.ERROR_TIMESTAMP);
    }
}
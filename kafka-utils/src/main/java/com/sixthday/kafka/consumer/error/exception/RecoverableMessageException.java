package com.sixthday.kafka.consumer.error.exception;

import lombok.Getter;

@Getter
public class RecoverableMessageException extends MessageException {
    private static final long serialVersionUID = 1L;

    public RecoverableMessageException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

    public RecoverableMessageException(String message, Throwable rootCause, boolean publishToRetryTopic) {
        super(message, rootCause, publishToRetryTopic);
    }

    public RecoverableMessageException(String message, Throwable rootCause, String topicName) {
        super(message, rootCause, topicName);
    }

    public RecoverableMessageException(String message, Throwable rootCause, boolean publishToRetryTopic, String topicName) {
        super(message, rootCause, publishToRetryTopic, topicName);
    }
}
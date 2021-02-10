package com.sixthday.kafka.consumer.error.exception;

import lombok.Getter;

@Getter
public class UnrecoverableMessageException extends MessageException {
    private static final long serialVersionUID = 1L;

    public UnrecoverableMessageException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

    public UnrecoverableMessageException(String message, Throwable rootCause, boolean publishToRetryTopic) {
        super(message, rootCause, publishToRetryTopic);
    }

    public UnrecoverableMessageException(String message, Throwable rootCause, String topicName) {
        super(message, rootCause, topicName);
    }

    public UnrecoverableMessageException(String message, Throwable rootCause, boolean publishToRetryTopic, String topicName) {
        super(message, rootCause, publishToRetryTopic, topicName);
    }
}
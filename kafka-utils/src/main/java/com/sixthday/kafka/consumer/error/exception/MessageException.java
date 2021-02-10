package com.sixthday.kafka.consumer.error.exception;

import lombok.Getter;

@Getter
public class MessageException extends Exception {
    private static final long serialVersionUID = 1L;
    private boolean publishMessageToTopic = true;
    private String topicName;
    private final Throwable actualCause;

    public MessageException(String message, Throwable actualCause) {
        super(message);
        this.actualCause = actualCause;
    }

    public MessageException(String message, Throwable actualCause, boolean publishMessageToTopic) {
        super(message);
        this.actualCause = actualCause;
        this.publishMessageToTopic = publishMessageToTopic;
    }

    public MessageException(String message, Throwable actualCause, String topicName) {
        super(message);
        this.actualCause = actualCause;
        this.topicName = topicName;
    }

    public MessageException(String message, Throwable actualCause, boolean publishMessageToTopic, String topicName) {
        super(message);
        this.actualCause = actualCause;
        this.publishMessageToTopic = publishMessageToTopic;
        this.topicName = topicName;
    }
}

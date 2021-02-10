package com.sixthday.kafka.consumer.error.listener;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MessageHeaderAttributes {
    public String RETRY_COUNT = "retry_count";
    public String EXCEPTION_CLASS = "exception_class";
    public String EXCEPTION_MESSAGE = "exception_msg";
    public String ROOT_CAUSE_EXCEPTION_CLASS = "root_cause_exception_class";
    public String ROOT_CAUSE_EXCEPTION_MESSAGE = "root_cause_exception_msg";
    public String TOPIC_NAME = "topic_name";
    public String MESSAGE_HISTORY = "history";
    public String ERROR_TIMESTAMP = "timestamp";
    public String OFFSET = "offset";
    public String PARTITION = "partition";
    public String GROUP_ID = "groupId";
}
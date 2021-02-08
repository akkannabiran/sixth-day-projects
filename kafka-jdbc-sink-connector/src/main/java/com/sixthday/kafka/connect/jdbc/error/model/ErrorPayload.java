package com.sixthday.kafka.connect.jdbc.error.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ErrorPayload {
    private Object key;
    private Object value;
    private Object history;
}

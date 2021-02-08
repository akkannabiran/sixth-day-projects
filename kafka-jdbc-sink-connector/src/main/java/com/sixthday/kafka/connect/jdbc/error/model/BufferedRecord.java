package com.sixthday.kafka.connect.jdbc.error.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.connect.sink.SinkRecord;

@Builder
@Getter
@Setter
public class BufferedRecord {
    private boolean isErrorProne;
    private SinkRecord sinkRecord;
    private Exception exception;
}

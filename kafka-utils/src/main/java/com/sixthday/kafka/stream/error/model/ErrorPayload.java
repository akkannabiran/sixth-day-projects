package com.sixthday.kafka.stream.error.model;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorPayload {
    private Object key;
    private Object value;
    private Object headers;
}

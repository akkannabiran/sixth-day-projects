package com.sixthday.logger.logging;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
@Builder
@Getter
public class LogEvent {
    public static final String FAILED = "Failed";
    public static final String SUCCESS = "Success";

    private String eventType;
    private String action;
    private String status;
    private String circuitBreakerStatus;
    private String exceptionClassName;
    private String exceptionMessage;
    private long duration;

    public void log() {
        log.info(
                new String()
                        .concat("event_type=\"" + eventType + "\", ")
                        .concat("action=\"" + action + "\", ")
                        .concat("status=\"" + status + "\", ")
                        .concat(StringUtils.isEmpty(circuitBreakerStatus) ? "" : "hystrix=\"" + circuitBreakerStatus + "\", ")
                        .concat(StringUtils.isEmpty(exceptionClassName) ? "" : "ClassName=\"" + exceptionClassName + "\", ")
                        .concat(StringUtils.isEmpty(exceptionMessage) ? "" : "msg=\"" + exceptionMessage + "\", ")
                        .concat("duration_millis=\"" + duration + "\"")
        );
    }
}

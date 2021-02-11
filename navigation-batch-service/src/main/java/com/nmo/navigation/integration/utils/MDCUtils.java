package com.sixthday.navigation.integration.utils;

import org.slf4j.MDC;

import java.util.Optional;

import static com.sixthday.sixthdayLogging.*;

public class MDCUtils {
    private MDCUtils() {
    }

    public static void setMDC(final String messageId, String messageType, String contextId, String originTimestamp, String source, String destination, String resource) {
        MDC.put(MDC_MESSAGE_ID, getDefinitiveValue(messageId, "-"));
        MDC.put(MDC_MESSAGE_TYPE, messageType);
        MDC.put(MDC_CONTEXT_ID, getDefinitiveValue(contextId, "-"));
        MDC.put(MDC_ORIGIN_TIMESTAMP, getDefinitiveValue(originTimestamp, "NA"));
        MDC.put(MDC_SOURCE_PROPERTY, getDefinitiveValue(source, "-"));
        MDC.put(MDC_DESTINATION_PROPERTY, getDefinitiveValue(destination, "-"));
        MDC.put(MDC_RESOURCE_PROPERTY, getDefinitiveValue(resource, "-"));
    }

    public static void setMDCOnMessageParsingFailure(String source, String destination, String resource) {
        MDC.put(MDC_MESSAGE_ID, "-");
        MDC.put(MDC_MESSAGE_TYPE, "DeserializationFailed");
        MDC.put(MDC_CONTEXT_ID, "-");
        MDC.put(MDC_ORIGIN_TIMESTAMP, "NA");
        MDC.put(MDC_SOURCE_PROPERTY, getDefinitiveValue(source, "-"));
        MDC.put(MDC_DESTINATION_PROPERTY, getDefinitiveValue(destination, "-"));
        MDC.put(MDC_RESOURCE_PROPERTY, getDefinitiveValue(resource, "-"));
    }

    private static <T> T getDefinitiveValue(T input, T defaultValue) {
        return Optional.ofNullable(input).orElse(defaultValue);
    }
}

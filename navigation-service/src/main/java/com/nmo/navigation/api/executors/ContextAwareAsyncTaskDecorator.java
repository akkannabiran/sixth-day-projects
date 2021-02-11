package com.sixthday.navigation.api.executors;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Map;

public class ContextAwareAsyncTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        final RequestAttributes currentRequestAttributes = RequestContextHolder.currentRequestAttributes();
        final Map<String, String> mdcContextMap = MDC.getCopyOfContextMap();
        return () -> {
            try {
                RequestContextHolder.setRequestAttributes(currentRequestAttributes);
                MDC.setContextMap(mdcContextMap);
                runnable.run();
            } finally {
                RequestContextHolder.resetRequestAttributes();
                MDC.clear();
            }
        };
    }
}
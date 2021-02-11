package com.sixthday.logger.logging;

import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandKey;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 50)
public class LoggingAspect {

    @Around("@within(com.sixthday.logger.logging.Loggable) || @annotation(com.sixthday.logger.logging.Loggable)")
    public Object logMethodEntryExit(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        String className = pjp.getSignature().getDeclaringTypeName();
        String methodName = pjp.getSignature().getName();
        if (log.isDebugEnabled()) {
        	log.debug("Entering method {}.{}(...)", className, methodName);
        }

        Object result = pjp.proceed();
        
        if (log.isDebugEnabled()) {
        	long elapsedTime = System.currentTimeMillis() - start;
        	log.debug("Exiting method {}.{}; execution time (ms): {}; response: {};", className, methodName, elapsedTime, result);
        }
        return result;
    }

    @Around("@annotation(loggableEvent)")
    public Object loggableEvent(ProceedingJoinPoint jp, LoggableEvent loggableEvent) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = jp.proceed();

            if (result instanceof ResponseEntity && isErrorCode((ResponseEntity) result)) {
                logEvent(loggableEvent, LogEvent.FAILED, System.currentTimeMillis() - start, null, null, null);
                return result;
            }
            logEvent(loggableEvent, LogEvent.SUCCESS, System.currentTimeMillis() - start, null, null, null);
            return result;
        } catch (Throwable throwable) {
            logEvent(loggableEvent, LogEvent.FAILED, System.currentTimeMillis() - start, getCircuitBreakerStatus(loggableEvent), throwable.getClass().getSimpleName(), throwable.getMessage());
            throw throwable;
        }
    }

    private boolean isErrorCode(ResponseEntity result) {
        return result.getStatusCode().is4xxClientError() || result.getStatusCode().is5xxServerError();
    }

    private void logEvent(LoggableEvent loggableEvent, String status, long elapsedTime, String circuitBreakerStatus, String exceptionClassName, String exceptionMessage) {
        LogEvent.builder()
                .eventType(loggableEvent.eventType())
                .action(loggableEvent.action())
                .status(status)
                .duration(elapsedTime)
                .circuitBreakerStatus(circuitBreakerStatus)
                .exceptionClassName(exceptionClassName)
                .exceptionMessage(exceptionMessage)
                .build()
                .log();
    }


    private String getCircuitBreakerStatus(LoggableEvent loggableEvent) {
        if ("UNKNOWN".equals(loggableEvent.hystrixCommandKey())) {
            return null;
        }
        HystrixCircuitBreaker circuitBreaker = HystrixCircuitBreaker.Factory.getInstance(HystrixCommandKey.Factory.asKey(loggableEvent.hystrixCommandKey()));
        if (circuitBreaker == null) {
            return "UNKNOWN";
        }
        return circuitBreaker.isOpen() ? "CIRCUIT_OPEN" : "UP";
    }
}

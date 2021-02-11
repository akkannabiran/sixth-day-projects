package com.sixthday.navigation.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("@within(com.sixthday.navigation.config.Loggable) || @annotation(com.sixthday.navigation.config.Loggable)")
    @SneakyThrows
    public Object logMethodEntryExit(ProceedingJoinPoint pjp) {

        long start = System.currentTimeMillis();

        String className = pjp.getSignature().getDeclaringTypeName();
        String methodName = pjp.getSignature().getName();

        Object[] args = pjp.getArgs();
        String argumentsToString = "";
        if (args != null) {
            argumentsToString = Arrays.stream(args)
                    .map(arg -> (arg == null) ? null : arg.toString())
                    .collect(Collectors.joining(","));
        }
        log.debug(String.format("Entering method %s.%s(%s)", className, methodName, argumentsToString));

        Object result = pjp.proceed();

        long elapsedTime = System.currentTimeMillis() - start;
        log.debug(String.format("Exiting method %s.%s; execution time (ms): %s", className, methodName, elapsedTime));
        return result;
    }
}

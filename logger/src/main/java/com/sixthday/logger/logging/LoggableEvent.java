package com.sixthday.logger.logging;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface LoggableEvent {

    String eventType() default "UNKNOWN";

    String action() default "UNKNOWN";

    String hystrixCommandKey() default "UNKNOWN";
}

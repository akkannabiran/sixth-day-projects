package com.toggler.core.toggles;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Toggle {

    String name();

    String fallback() default "";
}

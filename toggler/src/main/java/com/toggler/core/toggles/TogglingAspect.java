package com.toggler.core.toggles;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

@Slf4j
@Aspect
public class TogglingAspect {

    private final ApplicationContext applicationContext;

    @Autowired
    public TogglingAspect(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Around("@within(toggleAnnotation)")
    public Object toggleClass(ProceedingJoinPoint jp, Toggle toggleAnnotation) throws Throwable {
        String feature = toggleAnnotation.name();

        if (Feature.isDisabled(feature) && applicationContext.containsBean(toggleAnnotation.fallback())) {
            return invokeFallbackBean(jp, toggleAnnotation);
        }
        return jp.proceed();
    }

    @Around("@annotation(toggleAnnotation)")
    public Object toggleMethod(ProceedingJoinPoint jp, Toggle toggleAnnotation) throws Throwable {
        String feature = toggleAnnotation.name();

        if (Feature.isDisabled(feature)) {
            return invokeFallbackMethod(jp, toggleAnnotation);
        }
        return jp.proceed();
    }

    private Object invokeFallbackBean(ProceedingJoinPoint jp, Toggle toggleAnnotation) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method method = ((MethodSignature) jp.getSignature()).getMethod();
        Object fallbackBean = applicationContext.getBean(toggleAnnotation.fallback());
        return fallbackBean.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes()).invoke(fallbackBean, jp.getArgs());
    }

    private Object invokeFallbackMethod(ProceedingJoinPoint jp, Toggle toggleAnnotation) throws Throwable {
        Method method = ((MethodSignature) jp.getSignature()).getMethod();
        Optional<Method> fallbackMethodOption = Optional.ofNullable(method.getDeclaringClass().getDeclaredMethod(toggleAnnotation.fallback(), method.getParameterTypes()));
        if (fallbackMethodOption.isPresent()) {
            Method fallbackMethod = fallbackMethodOption.get();
            fallbackMethod.setAccessible(true);
            return fallbackMethod.invoke(jp.getThis(), jp.getArgs());
        }
        return jp.proceed();
    }
}

package com.sixthday.store.util;

import org.slf4j.MDC;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;


@Component
@Scope(SCOPE_SINGLETON)
public class sixthdayMDCAdapter {

    public String get(String key) {
        return MDC.get(key);
    }

    public void put(String key, String value) {
        MDC.put(key, value);
    }

    public void remove(String key) {
    	MDC.remove(key);
    }
    
    public void clear() {
        MDC.clear();
    }
}

package com.sixthday.kafka.stream.config.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
public class BeanUtil implements ApplicationContextAware {
    private static ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext pApplicationContext) throws BeansException {
        applicationContext = pApplicationContext;
    }

    public static <T> T getBean(String beanClass) {
        return (T) applicationContext.getBean(beanClass);
    }
}

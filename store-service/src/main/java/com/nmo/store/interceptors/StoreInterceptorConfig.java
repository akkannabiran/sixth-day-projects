package com.sixthday.store.interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@ComponentScan
@Configuration
public class StoreInterceptorConfig extends WebMvcConfigurerAdapter {

    @Autowired
    StoreRequestInterceptor storeRequestInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry interceptorRegistry) {
        interceptorRegistry.addInterceptor(storeRequestInterceptor);
    }
}

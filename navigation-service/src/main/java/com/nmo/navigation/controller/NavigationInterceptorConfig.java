package com.sixthday.navigation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@ComponentScan
@Configuration
public class NavigationInterceptorConfig extends WebMvcConfigurerAdapter {

    @Autowired
    NavigationRequestInterceptor navigationRequestInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(navigationRequestInterceptor);
    }
}

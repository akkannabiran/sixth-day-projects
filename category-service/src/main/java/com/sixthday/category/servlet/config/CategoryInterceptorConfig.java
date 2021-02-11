package com.sixthday.category.servlet.config;

import com.sixthday.category.servlet.CategoryRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@ComponentScan
@Configuration
public class CategoryInterceptorConfig extends WebMvcConfigurerAdapter {

    private CategoryRequestInterceptor categoryRequestInterceptor;

    @Autowired
    public CategoryInterceptorConfig(final CategoryRequestInterceptor categoryRequestInterceptor) {
        this.categoryRequestInterceptor = categoryRequestInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(categoryRequestInterceptor);
    }
}

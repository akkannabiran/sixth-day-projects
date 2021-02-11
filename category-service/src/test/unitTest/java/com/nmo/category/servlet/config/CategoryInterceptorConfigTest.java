package com.sixthday.category.servlet.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

@RunWith(MockitoJUnitRunner.class)
public class CategoryInterceptorConfigTest {

    @InjectMocks
    private CategoryInterceptorConfig categoryInterceptorConfig;

    @Mock
    private InterceptorRegistry interceptorRegistry;

    @Test
    public void addInterceptors() {
        categoryInterceptorConfig.addInterceptors(interceptorRegistry);
    }
}

package com.toggler.core.config;

import com.toggler.core.toggles.TogglingAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:app.properties")
public class TogglerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TogglingAspect.class)
    public TogglingAspect getTogglingAspect(ApplicationContext applicationContext) {
        return new TogglingAspect(applicationContext);
    }
}

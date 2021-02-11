package com.sixthday.store.config;

import com.toggler.core.toggles.Feature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

import static com.sixthday.store.toggles.Features.STUB_GOT_WWW;

@ComponentScan("com.sixthday.store")
@Configuration
public class GotWWWBeanConfiguration {

    private GotWWWConfig realGotWWWConfig;
    private StubGotWWWConfig stubGotWWWConfig;

    @Autowired
    public GotWWWBeanConfiguration(RealGotWWWConfig realGotWWWConfig, StubGotWWWConfig stubGotWWWConfig) {
        this.realGotWWWConfig = realGotWWWConfig;
        this.stubGotWWWConfig = stubGotWWWConfig;
    }

    @Bean
    @RequestScope
    @Qualifier("GOT_WWW_CONFIG")
    public GotWWWConfig getGotWWWConfig() {
        if (Feature.isEnabled(STUB_GOT_WWW)) {
            return stubGotWWWConfig;
        }
        return realGotWWWConfig;
    }
}

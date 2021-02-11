package com.sixthday.store.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration("stub-got-www")
@ConfigurationProperties(prefix = "sixthday-store-api.stub-got-www-api")
public class StubGotWWWConfig extends GotWWWConfig {
}

package com.sixthday.store.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration("real-got-www")
@ConfigurationProperties(prefix = "sixthday-store-api.got-www-api")
public class RealGotWWWConfig extends GotWWWConfig {
}

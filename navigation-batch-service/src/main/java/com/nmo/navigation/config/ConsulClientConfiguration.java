package com.sixthday.navigation.config;

import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.consul.ConsulProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@EnableConfigurationProperties
public class ConsulClientConfiguration {

    private final ConsulProperties consulProperties;

    @Autowired
    public ConsulClientConfiguration(ConsulProperties consulProperties) {
        this.consulProperties = consulProperties;
    }

    @Bean
    public KeyValueClient consulKeyValueClient() throws URISyntaxException {
        URI uri = new URIBuilder().setScheme("http").setHost(consulProperties.getHost()).setPort(consulProperties.getPort()).build();
        Consul consul = Consul.builder().withUrl(uri.toString()).build();
        return consul.keyValueClient();
    }
}

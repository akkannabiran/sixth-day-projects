package com.sixthday.navigation.repository;

import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
public abstract class WebRepository {

    @SneakyThrows
    public URI buildGetUrl(List<String> uriVars) {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(getServiceConfig().getScheme())
                .host(getServiceConfig().getHost())
                .port(getServiceConfig().getPort())
                .path(getServiceConfig().getServiceUrl())
                .build();
        String url = uriComponents.expand(uriVars.toArray()).toString();
        return new URI(url);
    }

    protected abstract NavigationBatchServiceConfig.ServiceConfig getServiceConfig();
}
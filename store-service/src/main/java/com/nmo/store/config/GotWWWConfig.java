package com.sixthday.store.config;

import com.sixthday.store.exceptions.URIConstructionException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Getter
@Setter
@Slf4j
public abstract class GotWWWConfig {
    private String scheme;
    private String host;
    private String path;
    private Integer port = 80;
    private Integer defaultMileRadius;
    private Integer resultLimit;

    public URI getUrl(List<NameValuePair> queryParameters) {
        try {
            return new URIBuilder()
                    .setScheme(scheme)
                    .setHost(host)
                    .setPort(port)
                    .setPath(path)
                    .setParameters(queryParameters)
                    .build();
        } catch (URISyntaxException e) {
            log.error("Error constructing got-www url from scheme:{} host:{}", scheme, host, e);
            throw new URIConstructionException("Error constructing got-www url");
        }
    }
}

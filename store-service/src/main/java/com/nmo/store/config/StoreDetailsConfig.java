package com.sixthday.store.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "sixthday-store-api")
@Getter
@Setter
@Slf4j
@ComponentScan(basePackages = {"com.sixthday.store.config", "com.sixthday.storeinventory"})
public class StoreDetailsConfig {

    private ElasticSearchConfig elasticSearchConfig;

    @Autowired
    private VaultSecrets vaultSecrets;

    @Bean
    public RestHighLevelClient elasticSearchClient() {
        log.info("START - Rest client initialization");

        HttpHost host = new HttpHost(elasticSearchConfig.getElasticSearchHost(), elasticSearchConfig.getElasticSearchPort()); // Add 3rd parameter "https" if necessary

        // Add creating of SSLContext sslContext if necessary

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        if (org.apache.commons.lang3.StringUtils.isNotBlank(vaultSecrets.getElasticSearch6User()) && org.apache.commons.lang3.StringUtils.isNotBlank(vaultSecrets.getElasticSearch6Password())) {
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(vaultSecrets.getElasticSearch6User(), vaultSecrets.getElasticSearch6Password()));
        } else {
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(vaultSecrets.getElasticSearch6User(), vaultSecrets.getElasticSearch6Password()));
        }

        log.info("END - Rest client initialization");

        return new RestHighLevelClient(
                RestClient.builder(host)
                        .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder//.setSSLContext(sslContext)
                                // .setSSLHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
                                .setDefaultCredentialsProvider(credentialsProvider))
        );

    }

    @Setter
    @Getter
    public static class ElasticSearchConfig {
        private String clusterName;
        private String clusterNodes;
        private String storeIndexName;
        private String storeSkuInventoryIndexName;
        private String elasticSearchHost;
        private int elasticSearchPort;
        private boolean enableTransportSniff;
    }

}

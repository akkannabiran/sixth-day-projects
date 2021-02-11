package com.sixthday.category.elasticsearch.config;

import com.sixthday.category.vault.config.VaultConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "category-service.elasticsearch")
@Getter
@Setter
@Slf4j
public class ElasticSearchConfig {

    private CategoryConfig categoryConfig;
    private VaultConfig vaultConfig;

    @Autowired
    public ElasticSearchConfig(final VaultConfig vaultConfig) {
        this.vaultConfig = vaultConfig;
    }

    @Bean(name = "CategoryHighLevelClient")
    @SneakyThrows
    public RestHighLevelClient elasticSearchClient() {
        HttpHost host = new HttpHost(getCategoryConfig().getEs6Host(), getCategoryConfig().getEs6Port());
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        if (StringUtils.isNotBlank(vaultConfig.getElasticsearch6().getUsername()) && StringUtils.isNotBlank(vaultConfig.getElasticsearch6().getPassword())) {
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(vaultConfig.getElasticsearch6().getUsername(), vaultConfig.getElasticsearch6().getPassword()));
        } else {
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(vaultConfig.getElasticsearch().getUsername(), vaultConfig.getElasticsearch().getPassword()));
        }
        return new RestHighLevelClient(RestClient.builder(host)
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                .setDefaultCredentialsProvider(credentialsProvider)));
    }

    @Setter
    @Getter
    public static class CategoryConfig {
        private String clusterName;
        private String indexName;
        private String documentType;
        private String host;
        private int port;
        private String es6Host;
        private int es6Port;
        private boolean enableTransportSniff;
    }

}

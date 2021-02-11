package com.sixthday.navigation.api.config;

import lombok.*;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "navigation")
@Getter
@Setter
@ComponentScan(basePackages = {"com.sixthday.navigation.api.config"})
public class NavigationServiceConfig {
    private ElasticSearchConfig elasticSearchConfig;
    private VaultConfig vaultConfig;
    private Storage storage;
    private CategoryConfig categoryConfig;
    private Integration integration;
    private ThreadPoolConfig threadPoolConfig;

    @Bean
    @SneakyThrows
    public RestHighLevelClient elasticSearchClient() {

        HttpHost host = new HttpHost(elasticSearchConfig.getHost6(), elasticSearchConfig.getPort6());

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        if (StringUtils.isNotBlank(vaultConfig.getElasticsearch6Username()) && StringUtils.isNotBlank(vaultConfig.getElasticsearch6Password())) {
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(vaultConfig.getElasticsearch6Username(), vaultConfig.getElasticsearch6Password()));
        } else {
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(vaultConfig.getElasticsearchUsername(), vaultConfig.getElasticsearchPassword()));
        }


        return new RestHighLevelClient(
                RestClient.builder(host)
                        .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                                .setDefaultCredentialsProvider(credentialsProvider))
        );
    }

    @Setter
    @Getter
    public static class ElasticSearchConfig {
        private String clusterName;
        private String clusterNodes;
        private String indexName;
        private String documentType;
        private String documentType6;
        private IndexConfig leftNavIndex;
        private String host;
        private int port;
        private String host6;
        private int port6;
        private boolean enableTransportSniff;

        @Setter
        @Getter
        public static class IndexConfig {
            private String name;
            private String documentType;
            private String documentType6;
        }
    }

    @Setter
    @Getter
    public static class VaultConfig {
        private String elasticsearchUsername;
        private String elasticsearchPassword;
        private String elasticsearch6Username;
        private String elasticsearch6Password;
        private String rabbitmqUsername;
        private String rabbitmqPassword;
    }

    @Setter
    @Getter
    public static class Storage {
        private S3 s3;

        @Setter
        @Getter
        public static class S3 {
            private String bucketName;
            private String clientRegion;
        }
    }

    @Getter
    @Setter
    public static class CategoryConfig {
        private String headerAssetUrl;
        private IdConfig idConfig;
        private List<FilterOption> filterOptions = new ArrayList<>();
        private List<String> reducedChildCountSilos;
        private Map<String, String> alternateDefaults = new HashMap<>();

        @Getter
        @Setter
        public static class IdConfig {
            private String live;
            private String seoFooter;
            private String designer;
            private String stage;
            private String marketing;
        }

        @Getter
        @Setter
        public static class FilterOption {
            private String filterKey;
            private String displayText;
        }
    }

    @Setter
    @Getter
    public static class Integration {
        @Setter
        @Getter
        public List<String> countryCodes;
    }
    
    @Getter
    @Setter
    public static class ThreadPoolConfig {
      private int coreSize;
    }
}

package com.sixthday.navigation.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "navigation")
@Getter
@Setter
@ComponentScan(basePackages = {"com.sixthday.navigation.config"})
public class NavigationBatchServiceConfig {
    private IntegrationConfig integration;
    private ElasticSearchConfig elasticSearchConfig;
    private VaultConfig vaultConfig;
    private LeftNavBatchConfig leftNavBatchConfig;
    private RabbitmqConfig rabbitmqConfig;
    private CategoryIdConfig categoryIdConfig;
    private LeftNavConfig leftNavConfig;
    private Redis redis;
    private Storage storage;

    @Bean(name = "mobileNavBatchConfig")
    public BatchConfig mobileNavBatchConfig() {
        return integration.getMobile();
    }

    @Bean(name = "ContentServiceRestTemplate")
    public RestTemplate contentServiceRestTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        return new RestTemplate(factory);
    }

    @Bean(name = "desktopNavBatchConfig")
    public BatchConfig desktopNavBatchConfig() {
        return integration.getDesktop();
    }

    @Bean
    public List<String> countryCodes() {
        return integration.getCountryCodes();
    }

    @Bean
    public List<String> navKeyGroups() {
        return integration.getNavKeyGroups();
    }

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
                        .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
        );
    }

    @Bean
    @Qualifier("elasticSearchObjectMapper")
    public ObjectMapper elasticSearchObjectMapper() {
        return new ObjectMapper().enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
                .setTimeZone(TimeZone.getTimeZone("America/Chicago"));
    }

    @Getter
    @Setter
    public static class Redis {
        CacheConfig designerIndex;
        CacheConfig leftNav;

        @Getter
        @Setter
        public static class CacheConfig {
            private boolean enableRedis;
            private String host;
            private int port;
            private boolean useSsl;
            private boolean useCluster;
            private boolean enablePeriodicRefresh;
            private long refreshPeriod;
            private boolean validateClusterNodeMembership;
        }
    }

    @Getter
    @Setter
    public static class Storage {
        S3 s3;

        @Getter
        @Setter
        public static class S3 {
            private String bucketName;
            private String clientRegion;
            private boolean enabled;
        }
    }

    @Getter
    @Setter
    public static class BatchConfig {
        private String url;
        private String userAgent;
        private String cron;
        private int connectTimeOut;
        private int readTimeOut;
    }

    @Getter
    @Setter
    public static class ServiceConfig {
        private String host;
        private int port;
        private String scheme;
        private String serviceUrl;
        private boolean enabled;
    }

    @Getter
    @Setter
    public static class LeftNavBatchConfig {
        private boolean buildOnEventReceiver;
        private boolean buildOnStartup;
        private int numberOfDocuments;
        private int scrollTimeout;
        private int writeBatchSize;
        private int tlvToIncludeReferenceIdsPathToRebuild;
        private int maxLeftnavTobeRebuild;
    }

    @Getter
    @Setter
    public static class LeftNavConfig {
        private String leftNavRefreshablePath;
    }

    @Getter
    @Setter
    public static class IntegrationConfig {
        private BatchConfig mobile;
        private BatchConfig desktop;
        private List<String> countryCodes;
        private List<String> navKeyGroups;
        private Map<String, String> categoryType;
        private ServiceConfig contentServiceConfig;
    }

    @Getter
    @Setter
    public static class ElasticSearchConfig {
        private String clusterName;
        private String clusterNodes;
        private IndexConfig categoryIndex;
        private IndexConfig leftNavIndex;
        private String host;
        private String host6;
        private int port;
        private int port6;
        private boolean enableTransportSniff;
    }

    @Getter
    @Setter
    public static class RabbitmqConfig {
        private Receiver receiver;

        @Getter
        @Setter
        public static class Receiver {
            ReceiverInfo categoryEvent;

            @Getter
            @Setter
            public static class ReceiverInfo {
                private String queueName;
                private String host;
                private int port;
                private int connectionTimeout;
                private int requestedHeartBeat = 60;
                private int concurrentConsumers = 1;
                private int prefetchCount = 1;
            }
        }
    }

    @Getter
    @Setter
    public static class CategoryIdConfig {
        private String designerCategoryId;
        private String designerByCategory;
    }

    @Setter
    @Getter
    public static class IndexConfig {
        private String name;
        private String documentType;
        private String documentType6;
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
}

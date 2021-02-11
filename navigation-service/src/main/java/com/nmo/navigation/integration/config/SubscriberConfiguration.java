package com.sixthday.navigation.integration.config;

import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.integration.receiver.CategoryMessageReceiver;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "navigation")
@Getter
@Setter
@Slf4j
public class SubscriberConfiguration {

    private RabbitmqConfig rabbitmqConfig;
    private NavigationServiceConfig navigationServiceConfig;

    SubscriberConfiguration(NavigationServiceConfig navigationServiceConfig) {
        this.navigationServiceConfig = navigationServiceConfig;
    }

    public RestHighLevelClient elasticSearchClient() {
        return navigationServiceConfig.elasticSearchClient();
    }

    public NavigationServiceConfig.ElasticSearchConfig getElasticSearchConfig() {
        return navigationServiceConfig.getElasticSearchConfig();
    }

    public NavigationServiceConfig.VaultConfig getVaultConfig() {
        return navigationServiceConfig.getVaultConfig();
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory(rabbitmqConfig.getHost());
        factory.setPort(rabbitmqConfig.getPort());
        factory.setUsername(getVaultConfig().getRabbitmqUsername());
        factory.setPassword(getVaultConfig().getRabbitmqPassword());
        factory.setConnectionTimeout(rabbitmqConfig.getConnectionTimeout());
        factory.setRequestedHeartBeat(rabbitmqConfig.getRequestedHeartBeat());
        log.info("Connecting to RabbitMQ at {}:{}", rabbitmqConfig.getHost(), rabbitmqConfig.getPort());
        return factory;
    }

    @Bean
    @Autowired
    public SimpleMessageListenerContainer listenerContainer(final CategoryMessageReceiver categoryMessageReceiver) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory());
        container.setQueueNames(rabbitmqConfig.getQueueName());
        container.setMessageListener(categoryMessageReceiver);
        container.setPrefetchCount(rabbitmqConfig.getPrefetchCount());
        container.setConcurrentConsumers(rabbitmqConfig.getConcurrentConsumers());
        return container;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public Queue categoryQueue() {
        return new Queue(rabbitmqConfig.getQueueName(), true, false, false);
    }

    @Getter
    @Setter
    public static class RabbitmqConfig {
        private String queueName;
        private String host;
        private int port;
        private int connectionTimeout;
        private int requestedHeartBeat = 60;
        private int concurrentConsumers = 1;
        private int prefetchCount = 1;
    }
}

package com.sixthday.store.config;

import com.sixthday.store.StoreMessageReceiver;
import com.sixthday.store.StoreSkuInventoryMessageReceiver;
import com.sixthday.store.config.StoreDetailsConfig.ElasticSearchConfig;
import com.sixthday.storeinventory.receiver.SkuStoresInventoryMessageReceiver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
import org.springframework.stereotype.Component;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "sixthday-store-sub")
@Getter
@Setter
public class SubscriberConfiguration {
    @Autowired
    StoreDetailsConfig storeDetailsConfig;

    @Autowired
    private QueueConfig queueConfig;
    
    private QueueConfig storeInventoryBySkuQueueConfig;

    @Autowired
    private VaultSecrets vaultSecrets;

    public RestHighLevelClient elasticSearchClient() {
        return storeDetailsConfig.elasticSearchClient();
    }

    public ElasticSearchConfig getElasticSearchConfig() {
        return storeDetailsConfig.getElasticSearchConfig();
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory(queueConfig.getHost());
        factory.setPort(queueConfig.getPort());
        factory.setUsername(vaultSecrets.getRabbitMqUsername());
        factory.setPassword(vaultSecrets.getRabbitMqPassword());
        factory.setConnectionTimeout(queueConfig.getConnectionTimeout());
        factory.setRequestedHeartBeat(queueConfig.getRequestedHeartBeat());
        return factory;
    }

    @Bean(name="listenerContainer")
    @Autowired
    public SimpleMessageListenerContainer listenerContainer(final StoreMessageReceiver messageReceiver) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory());
        container.setQueueNames(queueConfig.getQueueName());
        container.setMessageListener(messageReceiver);
        container.setConcurrentConsumers(queueConfig.getConcurrentConsumers());
        return container;
    }
    
    @Bean(name="listenerContainerForStoreSkuInvMessage")
    @Autowired
    public SimpleMessageListenerContainer listenerContainerForStoreSkuInvMessage(final StoreSkuInventoryMessageReceiver skuInvMessageReceiver) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory());
        container.setQueueNames(queueConfig.getStoreSkuInventoryQueueName());
        container.setMessageListener(skuInvMessageReceiver);
        container.setConcurrentConsumers(queueConfig.getConcurrentConsumers());
        return container;
    }
    
    @Bean(name="storeInvBySKUReceiver")
    @Autowired
    public SimpleMessageListenerContainer listenerContainerForStoreInvBySKUMessage(final SkuStoresInventoryMessageReceiver receiver) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory());
        container.setQueueNames(storeInventoryBySkuQueueConfig.getQueueName());
        container.setMessageListener(receiver);
        container.setConcurrentConsumers(storeInventoryBySkuQueueConfig.getConcurrentConsumers());
        return container;
    }
    

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public Queue storeDetailQueue() {
        return new Queue(queueConfig.getQueueName(), true, false, false);
    }
    
    @Bean
    public Queue storeSkuInventoryQueue() {
        return new Queue(queueConfig.getStoreSkuInventoryQueueName(), true, false, false);
    }

    @Bean
    public Queue storeInventoryBySkuDetailQueue() {
        return new Queue(storeInventoryBySkuQueueConfig.getQueueName(), true, false, false);
    }
    
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Component
    public static class QueueConfig {
        private String queueName;
        private String storeSkuInventoryQueueName;
        private String host;
        private int port;
        private int connectionTimeout;
        private int requestedHeartBeat = 60;
        private int concurrentConsumers=1;
    }
}

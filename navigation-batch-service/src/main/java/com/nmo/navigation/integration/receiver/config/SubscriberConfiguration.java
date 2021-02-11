package com.sixthday.navigation.integration.receiver.config;

import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import com.sixthday.navigation.integration.receiver.CategoryDocumentReceiver;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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

    private NavigationBatchServiceConfig navigationBatchServiceConfig;

    @Autowired
    SubscriberConfiguration(NavigationBatchServiceConfig navigationBatchServiceConfig) {
        this.navigationBatchServiceConfig = navigationBatchServiceConfig;
    }

    private NavigationBatchServiceConfig.VaultConfig getVaultConfig() {
        return navigationBatchServiceConfig.getVaultConfig();
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory(navigationBatchServiceConfig.getRabbitmqConfig().getReceiver().getCategoryEvent().getHost());
        factory.setPort(navigationBatchServiceConfig.getRabbitmqConfig().getReceiver().getCategoryEvent().getPort());
        factory.setUsername(getVaultConfig().getRabbitmqUsername());
        factory.setPassword(getVaultConfig().getRabbitmqPassword());
        factory.setConnectionTimeout(navigationBatchServiceConfig.getRabbitmqConfig().getReceiver().getCategoryEvent().getConnectionTimeout());
        factory.setRequestedHeartBeat(navigationBatchServiceConfig.getRabbitmqConfig().getReceiver().getCategoryEvent().getRequestedHeartBeat());
        return factory;
    }

    @Bean
    public SimpleMessageListenerContainer listenerContainer(final CategoryDocumentReceiver categoryDocumentReceiver) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory());
        container.setQueueNames(navigationBatchServiceConfig.getRabbitmqConfig().getReceiver().getCategoryEvent().getQueueName());
        container.setMessageListener(categoryDocumentReceiver);
        container.setPrefetchCount(navigationBatchServiceConfig.getRabbitmqConfig().getReceiver().getCategoryEvent().getPrefetchCount());
        container.setConcurrentConsumers(navigationBatchServiceConfig.getRabbitmqConfig().getReceiver().getCategoryEvent().getConcurrentConsumers());
        return container;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public Queue categoryQueue() {
        return new Queue(navigationBatchServiceConfig.getRabbitmqConfig().getReceiver().getCategoryEvent().getQueueName(), true, false, false);
    }

}

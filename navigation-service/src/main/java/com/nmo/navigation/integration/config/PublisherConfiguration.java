package com.sixthday.navigation.integration.config;

import com.sixthday.navigation.api.config.NavigationServiceConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
public class PublisherConfiguration {

    private RabbitmqConfig rabbitmqConfig;
    private NavigationServiceConfig navigationServiceConfig;

    PublisherConfiguration(NavigationServiceConfig navigationServiceConfig) {
        this.navigationServiceConfig = navigationServiceConfig;
    }

    public NavigationServiceConfig.VaultConfig getVaultConfig() {
        return navigationServiceConfig.getVaultConfig();
    }

    @Bean(name = "categoryEventPublisherConnectionFactory")
    public ConnectionFactory categoryEventPublisherConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory(getRabbitmqConfig().getPublisher().getCategoryEvent().getHost());
        factory.setPort(getRabbitmqConfig().getPublisher().getCategoryEvent().getPort());
        factory.setUsername(getVaultConfig().getRabbitmqUsername());
        factory.setPassword(getVaultConfig().getRabbitmqPassword());
        factory.setConnectionTimeout(getRabbitmqConfig().getPublisher().getCategoryEvent().getConnectionTimeout());
        log.info("Connecting to RabbitMQ at {}:{}", getRabbitmqConfig().getPublisher().getCategoryEvent().getHost(), getRabbitmqConfig().getPublisher().getCategoryEvent().getPort());
        return factory;
    }

    @Bean(name = "categoryEventPublisherRabbitTemplate")
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(categoryEventPublisherConnectionFactory());
        rabbitTemplate.setRoutingKey(getRabbitmqConfig().getPublisher().getCategoryEvent().getQueueName());
        return rabbitTemplate;
    }

    @Bean(name = "categoryEventPublisherQueue")
    public Queue categoryEventPublisherQueue() {
        return new Queue(rabbitmqConfig.getPublisher().getCategoryEvent().getQueueName(), true, false, false);
    }

    @Getter
    @Setter
    public static class RabbitmqConfig {
        private Publisher publisher;

        @Setter
        @Getter
        public static class Publisher {
            private CategoryEvent categoryEvent;

            @Setter
            @Getter
            public static class CategoryEvent {
                private boolean enabled;
                private String queueName;
                private String host;
                private int port;
                private int connectionTimeout;
            }
        }
    }
}

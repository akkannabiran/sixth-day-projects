package com.sixthday.navigation.listeners;

import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.cache.KVCache;
import com.orbitz.consul.model.kv.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ConsulClientListener implements ApplicationListener<ContextRefreshedEvent> {

    private static final String CONSUL_PATH = "environments/%s/config/data-services-enabled";
    private static final String ENV_UNIQUE_NAME = "ENV_UNIQUE_NAME";

    private SimpleMessageListenerContainer listenerContainer;
    private KVCache kvCache;

    @Autowired
    @SuppressWarnings("squid:S2095")
    private ConsulClientListener(KeyValueClient consulKeyValueClient,
                                 SimpleMessageListenerContainer listenerContainer) {

        this.listenerContainer = listenerContainer;
        this.kvCache = KVCache.newCache(consulKeyValueClient, "environments");
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!shouldAddConsulListener()) {
            return;
        }

        kvCache.addListener(newValues -> {
            Boolean shouldListenToRabbitMq = Optional.ofNullable(System.getenv(ENV_UNIQUE_NAME))
                    .map(environmentName -> newValues.get(String.format(CONSUL_PATH, environmentName)))
                    .flatMap(Value::getValueAsString)
                    .map(Boolean::valueOf)
                    .orElse(true);

            if (shouldListenToRabbitMq) {
                this.listenerContainer.start();
                log.info("Data services enabled: Listening to rabbit-mq started");
            } else {
                this.listenerContainer.stop();
                log.info("Data services disabled: Listening to rabbit-mq stopped");
            }
        });

        kvCache.start();
    }

    private boolean shouldAddConsulListener() {
        return kvCache.getListeners().isEmpty();
    }
}
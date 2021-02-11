package com.sixthday.store.listeners;

import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.cache.KVCache;
import com.orbitz.consul.model.kv.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    private KeyValueClient consulKeyValueClient;
    private SimpleMessageListenerContainer listenerContainer;
    private SimpleMessageListenerContainer listenerContainerForStoreSkuInvMessage;
    private KVCache kvCache;

    @Autowired
    @SuppressWarnings("squid:S2095")
    private ConsulClientListener(KeyValueClient consulKeyValueClient,
                                 @Qualifier("listenerContainer")
                                         SimpleMessageListenerContainer listenerContainer,
                                 @Qualifier("listenerContainerForStoreSkuInvMessage")
                                         SimpleMessageListenerContainer listenerContainerForStoreSkuInvMessage) {
        this.consulKeyValueClient = consulKeyValueClient;
        this.listenerContainer = listenerContainer;
        this.listenerContainerForStoreSkuInvMessage = listenerContainerForStoreSkuInvMessage;
        this.kvCache = KVCache.newCache(this.consulKeyValueClient, "environments");
    }

    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!shouldAddConsulListener()) {
            return;
        }

        this.kvCache.addListener(newValues -> {
            Boolean shouldListenToRabbitMq = Optional.ofNullable(System.getenv(ENV_UNIQUE_NAME))
                    .map(environmentName -> newValues.get(String.format(CONSUL_PATH, environmentName)))
                    .flatMap(Value::getValueAsString)
                    .map(Boolean::valueOf)
                    .orElse(true);


            if (shouldListenToRabbitMq) {
                listenerContainer.start();
                listenerContainerForStoreSkuInvMessage.start();
                log.info("Data services enabled: Listening to rabbit-mq started");
            } else {
                listenerContainer.stop();
                listenerContainerForStoreSkuInvMessage.stop();
                log.info("Data services disabled: Listening to rabbit-mq stopped");
            }

        });
        kvCache.start();
    }

    private boolean shouldAddConsulListener() {
        return kvCache.getListeners().isEmpty() &&
                (listenerContainer.isRunning() && listenerContainerForStoreSkuInvMessage.isRunning());
    }
}
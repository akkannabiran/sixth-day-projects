package com.sixthday.kafka.consumer.config;

import com.sixthday.kafka.consumer.error.listener.ConsumerAwareErrorHandlerImpl;
import com.sixthday.kafka.consumer.worker.KafkaListenerResumeWorkerThread;
import com.sixthday.kafka.consumer.config.util.ConsumerConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.support.GenericWebApplicationContext;

import javax.annotation.PostConstruct;
import java.util.Properties;

@Component
public class ConsumerConfig {

    private static final String CONTAINER_FACTORY_BEAN_ID_POSTFIX = "_ContainerFactory";
    private final Logger logger = LoggerFactory.getLogger(ConsumerConfig.class);

    private final ConsumerProperties consumerProperties;
    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private final GenericWebApplicationContext genericWebApplicationContext;
    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    public ConsumerConfig(ConsumerProperties consumerProperties, ThreadPoolTaskScheduler threadPoolTaskScheduler,
                          GenericWebApplicationContext genericWebApplicationContext, KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry) {
        this.consumerProperties = consumerProperties;
        this.threadPoolTaskScheduler = threadPoolTaskScheduler;
        this.genericWebApplicationContext = genericWebApplicationContext;
        this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
    }

    @PostConstruct
    public void init() {
        if (consumerProperties.getKafka() != null && !CollectionUtils.isEmpty(consumerProperties.getKafka().getListeners())) {
            consumerProperties.getKafka().getListeners().forEach((listenerKey, listenerValue) -> {
                ConsumerFactory<String, Object> consumerFactory = ConsumerConfigUtil.buildConsumerFactory(
                        consumerProperties.getKafka().getConsumer().getProperties(),
                        listenerValue.getConsumer().getProperties());
                ConcurrentKafkaListenerContainerFactory<String, Object> concurrentKafkaListenerContainerFactory = new ConcurrentKafkaListenerContainerFactory<>();
                concurrentKafkaListenerContainerFactory.setConsumerFactory(consumerFactory);

                updateContainerProperties(listenerValue.getConsumer(), concurrentKafkaListenerContainerFactory);
                logger.info("Container property updated for {}", listenerKey);

                setScheduler(concurrentKafkaListenerContainerFactory, listenerValue.getConsumer(), listenerKey);

                setErrorHandler(consumerProperties.getKafka().getProducer().getProperties(), listenerValue.getRetryProducer(), listenerValue.getDlProducer(), concurrentKafkaListenerContainerFactory);

                logger.info("Error handler created for {}", listenerKey);

                genericWebApplicationContext.registerBean(listenerKey + CONTAINER_FACTORY_BEAN_ID_POSTFIX, ConcurrentKafkaListenerContainerFactory.class, () -> concurrentKafkaListenerContainerFactory);
                logger.info("Bean {} crated and registered", (listenerKey + CONTAINER_FACTORY_BEAN_ID_POSTFIX));
            });
        } else {
            logger.error("No listener(s) are specified!");
        }
    }

    private void setScheduler(ConcurrentKafkaListenerContainerFactory<String, Object> concurrentKafkaListenerContainerFactory, ConsumerProperties.ListenerInfo consumer, String listenerKey) {
        if (consumer.getScheduleStart() != null) {
            concurrentKafkaListenerContainerFactory.getContainerProperties().setScheduler(threadPoolTaskScheduler);
            threadPoolTaskScheduler.schedule(new KafkaListenerResumeWorkerThread(listenerKey, consumer.getTopic(), consumer.getMessagePollStatusDelay(), kafkaListenerEndpointRegistry), new CronTrigger(consumer.getScheduleStart()));
            logger.info("Kafka listener resume and pause scheduler configured for listener {}", listenerKey);
        } else {
            logger.info("No scheduler configured for listener {}, the messages will be consumed in realtime", listenerKey);
        }
    }

    private void setErrorHandler(Properties properties, ConsumerProperties.ListenerInfo retryTopic, ConsumerProperties.ListenerInfo dlTopic, ConcurrentKafkaListenerContainerFactory<String, Object> concurrentKafkaListenerContainerFactory) {
        concurrentKafkaListenerContainerFactory.setErrorHandler(new ConsumerAwareErrorHandlerImpl(
                ConsumerConfigUtil.buildKafkaTemplate(genericWebApplicationContext, properties, retryTopic, false),
                ConsumerConfigUtil.buildKafkaTemplate(genericWebApplicationContext, properties, dlTopic, true),
                ConsumerConfigUtil.getTopicName(retryTopic),
                ConsumerConfigUtil.getTopicName(dlTopic)));
    }

    private void updateContainerProperties(ConsumerProperties.ListenerInfo mainListener, ConcurrentKafkaListenerContainerFactory<String, Object> concurrentKafkaListenerContainerFactory) {
        concurrentKafkaListenerContainerFactory.getContainerProperties().setSyncCommits(mainListener.isSyncCommit());
        concurrentKafkaListenerContainerFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.valueOf(mainListener.getAckMode()));
        //concurrentKafkaListenerContainerFactory.getContainerProperties().setCommitRetries(mainListener.getCommitRetries());
    }
}
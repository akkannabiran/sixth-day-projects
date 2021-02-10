package com.sixthday.kafka.consumer.worker;

import com.sixthday.kafka.consumer.interceptors.ConsumerInterceptorImpl;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;

import java.util.concurrent.TimeUnit;

public class KafkaListenerResumeWorkerThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(KafkaListenerResumeWorkerThread.class);
    private final long messagePollStatusDelay;
    private final String listenerId;
    private final String topic;
    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    public KafkaListenerResumeWorkerThread(String listenerId, String topic, long messagePollStatusDelay, KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry) {
        this.listenerId = listenerId;
        this.topic = topic;
        this.messagePollStatusDelay = messagePollStatusDelay;
        this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
    }

    @Override
    @SneakyThrows
    public void run() {

        MessageListenerContainer messageListenerContainer = kafkaListenerEndpointRegistry.getListenerContainer(listenerId);

        if (messageListenerContainer == null) {
            logger.error("No listener found with ID {} ", listenerId);
            return;
        }

        if (!messageListenerContainer.isRunning()) {
            messageListenerContainer.start();
            logger.info("Kafka listener {} was started", listenerId);
        }

        if (messageListenerContainer.isContainerPaused()) {
            messageListenerContainer.resume();
            logger.info("Kafka listener {} was resumed", listenerId);
        }

        do {
            ConsumerInterceptorImpl.concurrentHashMap.put(topic, false);
            logger.info("Pausing the thread for {} seconds to evaluate message consumption status from listener {}", messagePollStatusDelay, listenerId);
            TimeUnit.SECONDS.sleep(messagePollStatusDelay);
        } while (ConsumerInterceptorImpl.concurrentHashMap.get(topic));

        if (!messageListenerContainer.isContainerPaused() || !messageListenerContainer.isPauseRequested()) {
            messageListenerContainer.pause();
            logger.info("Kafka listener {} was paused", listenerId);
        }
    }
}

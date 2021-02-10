package com.sixthday.kafka.consumer.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.util.Collections;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConsumerConfigTest {
    @InjectMocks
    private ConsumerConfig consumerConfig;
    @Mock
    private ConsumerProperties consumerProperties;
    @Mock
    private GenericWebApplicationContext genericWebApplicationContext;

    @Test
    public void testInitWithValidData() {
        when(consumerProperties.getKafka()).thenReturn(buildKafka());
        consumerConfig.init();
        //verify(genericWebApplicationContext, times(1)).registerBean(eq("sample_ContainerFactory"), eq(ConcurrentKafkaListenerContainerFactory.class), any(ConcurrentKafkaListenerContainerFactory.class));
    }

    @Test
    public void testInitWithInValidData() {
        consumerConfig.init();
        verify(genericWebApplicationContext, times(0)).registerBean(eq("sample_ContainerFactory"), eq(ConcurrentKafkaListenerContainerFactory.class), any(ConcurrentKafkaListenerContainerFactory.class));
    }

    private ConsumerProperties.Kafka buildKafka() {
        ConsumerProperties.Kafka kafka = new ConsumerProperties.Kafka();

        ConsumerProperties.Listener listener = new ConsumerProperties.Listener();

        ConsumerProperties.ListenerInfo consumerListener = new ConsumerProperties.ListenerInfo();
        consumerListener.setTopic("consumer-topic");
        listener.setConsumer(consumerListener);

        ConsumerProperties.ListenerInfo dlProducer = new ConsumerProperties.ListenerInfo();
        dlProducer.setTopic("dl-topic");
        listener.setDlProducer(dlProducer);

        ConsumerProperties.ListenerInfo retryProducer = new ConsumerProperties.ListenerInfo();
        retryProducer.setTopic("dl-topic");
        listener.setRetryProducer(retryProducer);

        kafka.setListeners(Collections.singletonMap("sample", listener));

        ConsumerProperties.Consumer consumer = new ConsumerProperties.Consumer();
        consumer.setProperties(new Properties());
        kafka.setConsumer(consumer);

        ConsumerProperties.Producer producer = new ConsumerProperties.Producer();
        producer.setProperties(new Properties());
        kafka.setProducer(producer);

        kafka.setServiceAccountId("some-account-id");
        kafka.setClusterApiKey("some-api-key");
        kafka.setClusterApiSecret("some-api-secret");

        return kafka;
    }
}
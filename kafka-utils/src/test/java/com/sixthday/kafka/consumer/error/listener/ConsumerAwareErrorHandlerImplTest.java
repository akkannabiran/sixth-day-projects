package com.sixthday.kafka.consumer.error.listener;

import com.sixthday.kafka.consumer.error.exception.RecoverableMessageException;
import com.sixthday.kafka.consumer.error.exception.UnrecoverableMessageException;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.DeserializationException;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConsumerAwareErrorHandlerImplTest {
    @InjectMocks
    private ConsumerAwareErrorHandlerImpl consumerAwareErrorHandler;
    @Mock
    private KafkaTemplate<String, Object> retryKafkaTemplate;
    @Mock
    private KafkaTemplate<String, Object> dlKafkaTemplate;
    @Mock
    private Consumer<?, ?> consumer;

    @Test
    public void testHandleWithRecoverableMessageException() {
        RecoverableMessageException recoverableMessageException = new RecoverableMessageException("some-error", new RuntimeException("root-cause"));
        RuntimeException re = new RuntimeException("runtime exception", recoverableMessageException);

        ConsumerRecord<?, ?> consumerRecord = new ConsumerRecord<>("some-original-topic", 1, 100, "some-key", "some-value");

        consumerAwareErrorHandler.handle(re, consumerRecord, consumer);

        verify(consumer).commitSync();
    }

    @Test
    public void testHandleWithUnrecoverableMessageException() {
        UnrecoverableMessageException unrecoverableMessageException = new UnrecoverableMessageException("some-error", new RuntimeException("root-cause"));
        RuntimeException re = new RuntimeException("runtime exception", unrecoverableMessageException);

        ConsumerRecord<?, ?> consumerRecord = new ConsumerRecord<>("some-original-topic", 1, 100, "some-key", "some-value");

        consumerAwareErrorHandler.handle(re, consumerRecord, consumer);

        verify(consumer).commitSync();
    }

    @Test
    public void testHandleWithDeserializationException() {
        DeserializationException deserializationException = new DeserializationException("some-error", "some-value".getBytes(), false, new RuntimeException("root-cause"));
        RuntimeException re = new RuntimeException("runtime exception", deserializationException);
        ConsumerRecord<?, ?> consumerRecord = new ConsumerRecord<>("some-original-topic", 1, 100, "some-key", "some-value");

        consumerAwareErrorHandler.handle(re, consumerRecord, consumer);

        verify(consumer).commitSync();
    }

    @Test
    public void testHandleWithUnknownException() {
        NullPointerException nullPointerException = new NullPointerException("some-bad-code-error");
        ConsumerRecord<?, ?> consumerRecord = new ConsumerRecord<>("some-original-topic", 1, 100, "some-key", "some-value");

        consumerAwareErrorHandler.handle(nullPointerException, consumerRecord, consumer);

        verify(consumer).commitSync();
    }
}
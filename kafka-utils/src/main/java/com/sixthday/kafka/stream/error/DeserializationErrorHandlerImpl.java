package com.sixthday.kafka.stream.error;

import com.sixthday.kafka.stream.error.listener.StreamAwareErrorHandlerImpl;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.errors.DeserializationExceptionHandler;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class DeserializationErrorHandlerImpl implements DeserializationExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(DeserializationErrorHandlerImpl.class);
    private final DeserializationHandlerResponse deserializationHandlerResponse = DeserializationHandlerResponse.CONTINUE;
    private final StreamAwareErrorHandlerImpl streamAwareErrorHandlerImpl = new StreamAwareErrorHandlerImpl();

    @Override
    public void configure(Map<String, ?> map) {
    }

    @Override
    public DeserializationHandlerResponse handle(ProcessorContext processorContext, ConsumerRecord<byte[], byte[]> consumerRecord, Exception e) {
        logger.info("Exception Caught {} and returning the state {}", e.getMessage(), deserializationHandlerResponse);
        streamAwareErrorHandlerImpl.handle(consumerRecord, e);
        return deserializationHandlerResponse;
    }
}

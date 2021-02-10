package com.sixthday.kafka.stream.error;

import com.sixthday.kafka.stream.error.listener.StreamAwareErrorHandlerImpl;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.streams.errors.ProductionExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ProductionErrorHandlerImpl implements ProductionExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ProductionErrorHandlerImpl.class);
    private final ProductionExceptionHandlerResponse productionExceptionHandlerResponse = ProductionExceptionHandlerResponse.CONTINUE;
    private final StreamAwareErrorHandlerImpl streamAwareErrorHandlerImpl = new StreamAwareErrorHandlerImpl();

    @Override
    public void configure(Map<String, ?> map) {
    }

    @Override
    public ProductionExceptionHandlerResponse handle(ProducerRecord<byte[], byte[]> producerRecord, Exception e) {
        logger.info("Exception Caught {} and returning the state {}", e.getMessage(), productionExceptionHandlerResponse);
        streamAwareErrorHandlerImpl.handle(producerRecord, e);
        return productionExceptionHandlerResponse;
    }
}

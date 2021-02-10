package com.sixthday.kafka.app.interceptors;

import com.sixthday.kafka.app.interceptors.util.GUIDUtil;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

public class GUIDProducerInterceptorImpl implements ProducerInterceptor {


    @Override
    public ProducerRecord onSend(ProducerRecord producerRecord) {
        GUIDUtil.initGuid();
        return producerRecord;
    }

    @Override
    public void onAcknowledgement(RecordMetadata recordMetadata, Exception e) {
        GUIDUtil.destroyGuid();
    }

    @Override
    public void close() {
        GUIDUtil.destroyGuid();
    }

    @Override
    public void configure(Map<String, ?> map) {

    }
}

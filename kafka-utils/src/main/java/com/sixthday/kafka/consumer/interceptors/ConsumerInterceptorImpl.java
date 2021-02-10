package com.sixthday.kafka.consumer.interceptors;

import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConsumerInterceptorImpl implements ConsumerInterceptor {

    public static Map<String, Boolean> concurrentHashMap = new ConcurrentHashMap<>();

    @Override
    public ConsumerRecords<?, ?> onConsume(ConsumerRecords consumerRecords) {
        if (consumerRecords != null) {
            for (ConsumerRecord<?, ?> consumerRecord : (Iterable<ConsumerRecord<?, ?>>) consumerRecords) {
                concurrentHashMap.put(consumerRecord.topic(), true);
            }
        }
        return consumerRecords;
    }

    @Override
    public void close() {
    }

    @Override
    public void onCommit(Map map) {
    }

    @Override
    public void configure(Map<String, ?> map) {

    }
}

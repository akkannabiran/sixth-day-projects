package com.sixthday.kafka.app.interceptors;

import com.sixthday.kafka.app.interceptors.util.GUIDUtil;
import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.util.Map;

public class GUIDConsumerInterceptorImpl implements ConsumerInterceptor {

    @Override
    public ConsumerRecords<?, ?> onConsume(ConsumerRecords consumerRecords) {
        GUIDUtil.initGuid();
        return consumerRecords;
    }

    @Override
    public void close() {
        GUIDUtil.destroyGuid();
    }

    @Override
    public void onCommit(Map map) {
        GUIDUtil.destroyGuid();
    }

    @Override
    public void configure(Map<String, ?> map) {

    }
}

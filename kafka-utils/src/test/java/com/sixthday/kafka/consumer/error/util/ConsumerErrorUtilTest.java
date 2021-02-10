package com.sixthday.kafka.consumer.error.util;

import com.sixthday.kafka.consumer.error.exception.UnrecoverableMessageException;
import com.sixthday.kafka.consumer.error.model.ErrorPayload;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.support.serializer.DeserializationException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ConsumerErrorUtilTest {

    @Test
    public void testGetRetryCountWhenHeaderIsEmpty() {
        Assertions.assertEquals("1", ConsumerErrorUtil.getRetryCount(new HashMap<>()));
    }

    @Test
    public void testGetRetryCountWhenHeaderDoNotHaveRetryCount() {
        Map<String, byte[]> headersAsMap = new HashMap<>();
        headersAsMap.put("some-header", "1".getBytes());

        Assertions.assertEquals("1", ConsumerErrorUtil.getRetryCount(headersAsMap));
    }

    @Test
    public void testGetRetryCountWhenHeaderHaveRetryCountWithValue0() {
        Map<String, byte[]> headersAsMap = new HashMap<>();
        headersAsMap.put("retry_count", "0".getBytes());

        Assertions.assertEquals("1", ConsumerErrorUtil.getRetryCount(headersAsMap));
    }

    @Test
    public void testGetRetryCountWhenHeaderHaveRetryCountWithValue1() {
        Map<String, byte[]> headersAsMap = new HashMap<>();
        headersAsMap.put("retry_count", "1".getBytes());

        Assertions.assertEquals("2", ConsumerErrorUtil.getRetryCount(headersAsMap));
    }

    @Test
    public void testGetHeaderAsMapWithStringValueWhenHeaderIsNull() {
        Map<String, Object> headerMap = ConsumerErrorUtil.getHeaderAsMapWithStringValue(null);
        assertNotNull(headerMap);
        assertEquals(0, headerMap.size());
    }

    @Test
    public void testGetHeaderAsMapWithStringValueWhenHeaderIsNotNull() {
        Headers headers = new RecordHeaders();
        headers.add(new RecordHeader("some-header", "100".getBytes()));
        headers.add(new RecordHeader("history", "[]".getBytes()));

        Map<String, Object> headerMap = ConsumerErrorUtil.getHeaderAsMapWithStringValue(headers);

        assertNotNull(headerMap);
        assertEquals(2, headerMap.size());
    }

    @Test
    public void testGetStringFromByteArrayWhenBytesIsNull() {
        assertNull(ConsumerErrorUtil.getStringFromByteArray(null));
    }

    @Test
    public void testGetStringFromByteArrayWhenBytesIsNotNull() {
        Assertions.assertEquals("\"10\"",
                ConsumerErrorUtil.getStringFromByteArray("10".getBytes()));
    }

    @Test
    public void testGetStringFromObjectWhenObjectIsNull() {
        Assertions.assertEquals("null", ConsumerErrorUtil.getStringFromObject(null));
    }

    @Test
    public void testGetStringFromObjectWhenObjectIsValidJson() {
        Assertions.assertEquals("\"100\"", ConsumerErrorUtil.getStringFromObject("100"));
    }

    @Test
    public void testGetStringFromObjectWhenObjectIsInvalidJson() {
        Assertions.assertEquals("\"100\"", ConsumerErrorUtil.getStringFromObject("100"));
    }

    @Test
    public void testMessageDispatchWhenTopicIsNullAndFalse() {
        Assertions.assertFalse(ConsumerErrorUtil.messageDispatch(null, false));
    }

    @Test
    public void testMessageDispatchWhenTopicIsNullAndTrue() {
        Assertions.assertFalse(ConsumerErrorUtil.messageDispatch(null, true));
    }

    @Test
    public void testMessageDispatchWhenTopicIsNotNullAndFalse() {
        Assertions.assertFalse(ConsumerErrorUtil.messageDispatch("some-topic", false));
    }

    @Test
    public void testMessageDispatchWhenTopicIsNotNullAndTrue() {
        Assertions.assertTrue(ConsumerErrorUtil.messageDispatch("some-topic", true));
    }

    @Test
    public void testBuildMessageWhenExceptionIsNotDeserialization() {
        Headers headers = new RecordHeaders();
        headers.add(new RecordHeader("some-header", "100".getBytes()));
        headers.add(new RecordHeader("history", "[]".getBytes()));
        headers.add(new RecordHeader("retry_count", "1".getBytes()));

        ConsumerRecord<?, ?> consumerRecord = new ConsumerRecord<>("some-original-topic", 1, 100, System.currentTimeMillis(), TimestampType.CREATE_TIME, 0L, 0, 0, "some-key", "some-value", headers);
        ProducerRecord<String, Object> producerRecord = ConsumerErrorUtil.buildProducerRecord(new ListenerExecutionFailedException(null, "my-group", null), new UnrecoverableMessageException("some-network-issue", new RuntimeException("some error")), new RuntimeException("some error"), consumerRecord, "some-topic", false);
        assertNotNull(producerRecord);
        assertEquals("some-value", producerRecord.value());
        assertEquals("some-key", producerRecord.key());
        assertEquals("2", new String(producerRecord.headers().lastHeader("retry_count").value()));
        assertEquals("com.carefirst.kafka.consumer.error.exception.UnrecoverableMessageException", new String(producerRecord.headers().lastHeader("exception_class").value()));
        assertEquals("some-network-issue", new String(producerRecord.headers().lastHeader("exception_msg").value()));
        assertEquals("java.lang.RuntimeException", new String(producerRecord.headers().lastHeader("root_cause_exception_class").value()));
        assertEquals("some error", new String(producerRecord.headers().lastHeader("root_cause_exception_msg").value()));
        assertEquals("some-original-topic", new String(producerRecord.headers().lastHeader("topic_name").value()));
        assertEquals("my-group", new String(producerRecord.headers().lastHeader("groupId").value()));
        producerRecord.headers().lastHeader("timestamp").value();
    }

    @Test
    public void testBuildMessageWhenExceptionIsNotDeserializationAndEnhancePayload() {
        Headers headers = new RecordHeaders();
        headers.add(new RecordHeader("some-header", "100".getBytes()));
        headers.add(new RecordHeader("history", "[]".getBytes()));
        headers.add(new RecordHeader("retry_count", "1".getBytes()));

        ConsumerRecord<?, ?> consumerRecord = new ConsumerRecord<>("some-original-topic", 1, 100, System.currentTimeMillis(), TimestampType.CREATE_TIME, 0L, 0, 0, "some-key", "some-value", headers);
        ProducerRecord<String, Object> producerRecord = ConsumerErrorUtil.buildProducerRecord(new ListenerExecutionFailedException(null, null, null), new UnrecoverableMessageException("some-network-issue", new RuntimeException("some error")), new RuntimeException("some error"), consumerRecord, "some-topic", true);
        assertNotNull(producerRecord);
        assertEquals("some-value", ((ErrorPayload) producerRecord.value()).getValue());
        assertEquals("some-key", ((ErrorPayload) producerRecord.value()).getKey());
        assertNotNull(((ErrorPayload) producerRecord.value()).getHeaders());
        assertEquals("2", new String(producerRecord.headers().lastHeader("retry_count").value()));
        assertEquals("com.carefirst.kafka.consumer.error.exception.UnrecoverableMessageException", new String(producerRecord.headers().lastHeader("exception_class").value()));
        assertEquals("some-network-issue", new String(producerRecord.headers().lastHeader("exception_msg").value()));
        assertEquals("java.lang.RuntimeException", new String(producerRecord.headers().lastHeader("root_cause_exception_class").value()));
        assertEquals("some error", new String(producerRecord.headers().lastHeader("root_cause_exception_msg").value()));
        assertEquals("some-original-topic", new String(producerRecord.headers().lastHeader("topic_name").value()));
        assertEquals("NO_GROUP_ID", new String(producerRecord.headers().lastHeader("groupId").value()));
        producerRecord.headers().lastHeader("timestamp").value();
    }

    @Test
    public void testBuildMessageWhenExceptionIsKeyDeserialization() {
        ConsumerRecord<?, ?> consumerRecord = new ConsumerRecord<>("some-original-topic", 1, 100, "some-key", "some-value");
        DeserializationException deserializationException = new DeserializationException("some error", "keyFromException".getBytes(), true, new Exception());
        ProducerRecord<String, Object> producerRecord = ConsumerErrorUtil.buildProducerRecord(new ListenerExecutionFailedException(null, "my-group", null), deserializationException, deserializationException, consumerRecord, "some-topic", false);
        assertEquals("some-value", producerRecord.value());
        assertEquals("keyFromException", producerRecord.key());
        assertEquals("1", new String(producerRecord.headers().lastHeader("retry_count").value()));
        assertEquals("org.springframework.kafka.support.serializer.DeserializationException", new String(producerRecord.headers().lastHeader("exception_class").value()));
        assertEquals("some error; nested exception is java.lang.Exception", new String(producerRecord.headers().lastHeader("exception_msg").value()));
        assertEquals("org.springframework.kafka.support.serializer.DeserializationException", new String(producerRecord.headers().lastHeader("root_cause_exception_class").value()));
        assertEquals("some error; nested exception is java.lang.Exception", new String(producerRecord.headers().lastHeader("root_cause_exception_msg").value()));
        assertEquals("some-original-topic", new String(producerRecord.headers().lastHeader("topic_name").value()));
        assertEquals("my-group", new String(producerRecord.headers().lastHeader("groupId").value()));
        producerRecord.headers().lastHeader("timestamp").value();
    }

    @Test
    public void testBuildMessageWhenExceptionIsKeyDeserializationAndEnhancePayload() {
        ConsumerRecord<?, ?> consumerRecord = new ConsumerRecord<>("some-original-topic", 1, 100, "some-key", "some-value");
        DeserializationException deserializationException = new DeserializationException("some error", "keyFromException".getBytes(), true, new Exception());
        ProducerRecord<String, Object> producerRecord = ConsumerErrorUtil.buildProducerRecord(new Exception("my-group"), deserializationException, deserializationException, consumerRecord, "some-topic", true);
        assertEquals("some-value", ((ErrorPayload) producerRecord.value()).getValue());
        assertEquals("keyFromException", ((ErrorPayload) producerRecord.value()).getKey());
        assertNotNull(((ErrorPayload) producerRecord.value()).getHeaders());
        assertEquals("1", new String(producerRecord.headers().lastHeader("retry_count").value()));
        assertEquals("org.springframework.kafka.support.serializer.DeserializationException", new String(producerRecord.headers().lastHeader("exception_class").value()));
        assertEquals("some error; nested exception is java.lang.Exception", new String(producerRecord.headers().lastHeader("exception_msg").value()));
        assertEquals("org.springframework.kafka.support.serializer.DeserializationException", new String(producerRecord.headers().lastHeader("root_cause_exception_class").value()));
        assertEquals("some error; nested exception is java.lang.Exception", new String(producerRecord.headers().lastHeader("root_cause_exception_msg").value()));
        assertEquals("some-original-topic", new String(producerRecord.headers().lastHeader("topic_name").value()));
        assertEquals("NO_GROUP_ID", new String(producerRecord.headers().lastHeader("groupId").value()));
        producerRecord.headers().lastHeader("timestamp").value();
    }

    @Test
    public void testBuildMessageWhenExceptionIsValueDeserialization() {
        ConsumerRecord<?, ?> consumerRecord = new ConsumerRecord<>("some-original-topic", 1, 100, "some-key", "some-value");
        DeserializationException deserializationException = new DeserializationException("some error", "valueFromException".getBytes(), false, new Exception());
        ProducerRecord<String, Object> producerRecord = ConsumerErrorUtil.buildProducerRecord(new ListenerExecutionFailedException(null, "my-group", null), deserializationException, deserializationException, consumerRecord, "some-topic", false);
        assertEquals("valueFromException", producerRecord.value());
        assertEquals("some-key", producerRecord.key());
        assertEquals("1", new String(producerRecord.headers().lastHeader("retry_count").value()));
        assertEquals("org.springframework.kafka.support.serializer.DeserializationException", new String(producerRecord.headers().lastHeader("exception_class").value()));
        assertEquals("some error; nested exception is java.lang.Exception", new String(producerRecord.headers().lastHeader("exception_msg").value()));
        assertEquals("org.springframework.kafka.support.serializer.DeserializationException", new String(producerRecord.headers().lastHeader("root_cause_exception_class").value()));
        assertEquals("some error; nested exception is java.lang.Exception", new String(producerRecord.headers().lastHeader("root_cause_exception_msg").value()));
        assertEquals("some-original-topic", new String(producerRecord.headers().lastHeader("topic_name").value()));
        assertEquals("my-group", new String(producerRecord.headers().lastHeader("groupId").value()));
        producerRecord.headers().lastHeader("timestamp").value();
    }

    @Test
    public void testBuildMessageWhenExceptionIsValueDeserializationAndEnhancePayload() {
        ConsumerRecord<?, ?> consumerRecord = new ConsumerRecord<>("some-original-topic", 1, 100, "some-key", "some-value");
        DeserializationException deserializationException = new DeserializationException("some error", "valueFromException".getBytes(), false, new Exception());
        ProducerRecord<String, Object> producerRecord = ConsumerErrorUtil.buildProducerRecord(new ListenerExecutionFailedException(null, "my-group", null), deserializationException, deserializationException, consumerRecord, "some-topic", true);
        assertEquals("valueFromException", ((ErrorPayload) producerRecord.value()).getValue());
        assertEquals("some-key", ((ErrorPayload) producerRecord.value()).getKey());
        assertNotNull(((ErrorPayload) producerRecord.value()).getHeaders());
        assertEquals("1", new String(producerRecord.headers().lastHeader("retry_count").value()));
        assertEquals("org.springframework.kafka.support.serializer.DeserializationException", new String(producerRecord.headers().lastHeader("exception_class").value()));
        assertEquals("some error; nested exception is java.lang.Exception", new String(producerRecord.headers().lastHeader("exception_msg").value()));
        assertEquals("org.springframework.kafka.support.serializer.DeserializationException", new String(producerRecord.headers().lastHeader("root_cause_exception_class").value()));
        assertEquals("some error; nested exception is java.lang.Exception", new String(producerRecord.headers().lastHeader("root_cause_exception_msg").value()));
        assertEquals("some-original-topic", new String(producerRecord.headers().lastHeader("topic_name").value()));
        assertEquals("my-group", new String(producerRecord.headers().lastHeader("groupId").value()));
        producerRecord.headers().lastHeader("timestamp").value();
    }
}
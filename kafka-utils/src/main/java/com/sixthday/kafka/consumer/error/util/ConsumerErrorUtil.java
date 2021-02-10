package com.sixthday.kafka.consumer.error.util;

import com.sixthday.kafka.consumer.error.listener.MessageHeaderAttributes;
import com.sixthday.kafka.consumer.error.model.ErrorPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.util.StringUtils;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@UtilityClass
public class ConsumerErrorUtil {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean messageDispatch(String topicName, boolean publishMessage) {
        return !StringUtils.isEmpty(topicName) && publishMessage;
    }

    public String getStringFromObject(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("Unable to parse the object {}", data);
        }
        return null;
    }

    public String getStringFromByteArray(byte[] data) {
        try {
            return objectMapper.writeValueAsString(new String(data));
        } catch (Exception e) {
            log.error("Unable to parse the bytes {}", data);
        }
        return null;
    }

    public ProducerRecord<String, Object> buildProducerRecord(Exception actualException, Throwable cause, Throwable rootCause, ConsumerRecord<?, ?> consumerRecord, String topicName, boolean enhancePayload) {
        String key = getKey(rootCause, consumerRecord);
        Object payload = getPayload(rootCause, consumerRecord);
        Headers headers = getHeaders(actualException, cause, rootCause, consumerRecord);
        if (enhancePayload) {
            return new ProducerRecord<>(
                    topicName, null, System.currentTimeMillis(), key, enhancePayload(key, payload, headers), headers);
        } else {
            return new ProducerRecord<>(
                    topicName, null, System.currentTimeMillis(), key, payload, headers);
        }
    }

    public Map<String, Object> getHeaderAsMapWithStringValue(Headers headers) {
        Map<String, Object> headerMap = new HashMap<>();
        if (headers != null) {
            headers.forEach(header -> {
                if (header.key().equals(MessageHeaderAttributes.MESSAGE_HISTORY)) {
                    headerMap.put(header.key(), convertByteArrayToList(header.value()));
                } else {
                    headerMap.put(header.key(), new String(header.value()));
                }
            });
        }
        return headerMap;
    }

    public String getRetryCount(Map<String, byte[]> headerAsMap) {
        if (headerAsMap.get(MessageHeaderAttributes.RETRY_COUNT) != null) {
            return Integer.valueOf(Integer.parseInt(new String(headerAsMap.get(MessageHeaderAttributes.RETRY_COUNT))) + 1).toString();
        }
        return Integer.valueOf(1).toString();
    }

    private Object enhancePayload(String key, Object payload, Headers headers) {
        return ErrorPayload.builder().key(key).value(payload).headers(getHeaderAsMapWithStringValue(headers)).build();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> convertByteArrayToList(byte[] bytes) {
        try {
            if (bytes != null) {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                return (List<Map<String, Object>>) objectInputStream.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            log.error("Exception caught while converting byte array to List");
        }
        return new ArrayList<>();
    }

    private Map<String, Object> convertByteMapToObjectMap(Map<String, byte[]> byteArrayMap) {
        Map<String, Object> objectMap = new HashMap<>();
        if (byteArrayMap != null) {
            byteArrayMap.forEach((key, value) -> objectMap.put(key, new String(value)));
        }
        return objectMap;
    }

    private byte[] convertListToByteArray(List<Map<String, Object>> history) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(history);
        } catch (IOException e) {
            log.error("Exception caught while converting List to byte array");
        }
        return byteArrayOutputStream.toByteArray();
    }

    @SneakyThrows
    private Headers getHeaders(Exception actualException, Throwable cause, Throwable rootCause, ConsumerRecord<?, ?> consumerRecord) {
        RecordHeaders headers = new RecordHeaders();
        Map<String, byte[]> headerAsMap = copyHeadersAndGetAsMap(headers, consumerRecord.headers());
        headers.add(new RecordHeader(MessageHeaderAttributes.GROUP_ID, getGroupId(actualException).getBytes()));
        headers.add(new RecordHeader(MessageHeaderAttributes.TOPIC_NAME, consumerRecord.topic().getBytes()));
        headers.add(new RecordHeader(MessageHeaderAttributes.EXCEPTION_CLASS, cause.getClass().getName().getBytes()));
        headers.add(new RecordHeader(MessageHeaderAttributes.EXCEPTION_MESSAGE, cause.getMessage() != null ? cause.getMessage().getBytes() : "NO_ERROR_MESSAGE".getBytes()));
        headers.add(new RecordHeader(MessageHeaderAttributes.ROOT_CAUSE_EXCEPTION_CLASS, rootCause.getClass().getName().getBytes()));
        headers.add(new RecordHeader(MessageHeaderAttributes.ROOT_CAUSE_EXCEPTION_MESSAGE, rootCause.getMessage() != null ? rootCause.getMessage().getBytes() : "NO_ERROR_MESSAGE".getBytes()));
        headers.add(new RecordHeader(MessageHeaderAttributes.RETRY_COUNT, getRetryCount(headerAsMap).getBytes()));
        headers.add(new RecordHeader(MessageHeaderAttributes.OFFSET, Long.toString(consumerRecord.offset()).getBytes()));
        headers.add(new RecordHeader(MessageHeaderAttributes.PARTITION, Integer.toString(consumerRecord.partition()).getBytes()));
        headers.add(new RecordHeader(MessageHeaderAttributes.MESSAGE_HISTORY, getHistory(headerAsMap)));
        headers.add(new RecordHeader(MessageHeaderAttributes.ERROR_TIMESTAMP, LocalDateTime.now().toString().getBytes()));
        return headers;
    }

    private String getGroupId(Exception actualException) {
        String groupId = "NO_GROUP_ID";
        if ((actualException instanceof ListenerExecutionFailedException) &&
                ((ListenerExecutionFailedException) actualException).getGroupId() != null)
            return ((ListenerExecutionFailedException) actualException).getGroupId();
        return groupId;
    }

    private Map<String, byte[]> copyHeadersAndGetAsMap(Headers newHeader, Headers existingHeader) {
        Map<String, byte[]> headerAsMap = new HashMap<>();
        if (existingHeader != null) {
            existingHeader.forEach(header -> {
                headerAsMap.put(header.key(), header.value());
                if (!header.key().equals(MessageHeaderAttributes.GROUP_ID) &&
                        !header.key().equals(MessageHeaderAttributes.TOPIC_NAME) &&
                        !header.key().equals(MessageHeaderAttributes.EXCEPTION_CLASS) &&
                        !header.key().equals(MessageHeaderAttributes.EXCEPTION_MESSAGE) &&
                        !header.key().equals(MessageHeaderAttributes.ROOT_CAUSE_EXCEPTION_CLASS) &&
                        !header.key().equals(MessageHeaderAttributes.ROOT_CAUSE_EXCEPTION_MESSAGE) &&
                        !header.key().equals(MessageHeaderAttributes.RETRY_COUNT) &&
                        !header.key().equals(MessageHeaderAttributes.OFFSET) &&
                        !header.key().equals(MessageHeaderAttributes.PARTITION) &&
                        !header.key().equals(MessageHeaderAttributes.MESSAGE_HISTORY) &&
                        !header.key().equals(MessageHeaderAttributes.ERROR_TIMESTAMP)) {
                    newHeader.add(new RecordHeader(header.key(), header.value()));
                }
            });
        }
        return headerAsMap;
    }

    private byte[] getHistory(Map<String, byte[]> headerAsMap) {
        List<Map<String, Object>> history = convertByteArrayToList(headerAsMap.get(MessageHeaderAttributes.MESSAGE_HISTORY));
        headerAsMap.remove(MessageHeaderAttributes.MESSAGE_HISTORY);
        history.add(convertByteMapToObjectMap(headerAsMap));
        return convertListToByteArray(history);
    }

    private String getKey(Throwable e, ConsumerRecord<?, ?> consumerRecord) {
        return (e instanceof DeserializationException &&
                ((DeserializationException) e).isKey()) ? new String(((DeserializationException) e).getData()) : (String) consumerRecord.key();
    }

    private Object getPayload(Throwable e, ConsumerRecord<?, ?> consumerRecord) {
        return (e instanceof DeserializationException &&
                !((DeserializationException) e).isKey()) ? new String(((DeserializationException) e).getData()) : consumerRecord.value();
    }
}
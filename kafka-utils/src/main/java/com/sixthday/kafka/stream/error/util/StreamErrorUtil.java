package com.sixthday.kafka.stream.error.util;

import com.sixthday.kafka.consumer.error.listener.MessageHeaderAttributes;
import com.sixthday.kafka.consumer.error.model.ErrorPayload;
import com.sixthday.kafka.stream.config.StreamProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@UtilityClass
public class StreamErrorUtil {

    private final Logger logger = LoggerFactory.getLogger(StreamErrorUtil.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getStringFromObject(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("Unable to parse the object {}", data);
        }
        return null;
    }

    public String getDlTopic(StreamProperties streamProperties, String topic) {
        return streamProperties.getKafka().getStream().getApps().entrySet().parallelStream().filter(stringStreamInfoEntry ->
                (topic.equals(stringStreamInfoEntry.getValue().getSourceTopic()) || topic.equals(stringStreamInfoEntry.getValue().getTargetTopic())))
                .findFirst().orElse(streamProperties.getKafka().getStream().getApps().entrySet().iterator().next()).getValue().getDlTopic();
    }

    public Object deserialize(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        try (ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Exception caught while converting byte array to object {}", e.getMessage());
            return new String(bytes);
        }
    }

    public ProducerRecord<Object, Object> buildProducerRecord(StreamProperties streamProperties, ConsumerRecord<byte[], byte[]> consumerRecord, Exception e) {
        return buildProducerRecord(streamProperties, e, consumerRecord.headers(), consumerRecord.partition(), consumerRecord.offset(), consumerRecord.topic()
                , deserialize(consumerRecord.key()), deserialize(consumerRecord.value()));
    }

    public ProducerRecord<Object, Object> buildProducerRecord(StreamProperties streamProperties, ProducerRecord<byte[], byte[]> producerRecord, Exception e) {
        return buildProducerRecord(streamProperties, e, producerRecord.headers(), producerRecord.partition(), -1, producerRecord.topic()
                , deserialize(producerRecord.key()), deserialize(producerRecord.value()));
    }

    public static ProducerRecord<Object, Object> buildProducerRecord(StreamProperties streamProperties, String sourceTopic, Object key, Object value, Exception e) {
        return buildProducerRecord(streamProperties, e, null, -1, -1, sourceTopic, key, value);
    }

    public ProducerRecord<Object, Object> buildProducerRecord(StreamProperties streamProperties, Exception e, Headers existingHeaders, int partition, long offset, String topic, Object key, Object value) {
        Headers headers = getHeaders(e, existingHeaders, partition, offset, topic);
        return new ProducerRecord<>(
                getDlTopic(streamProperties, topic),
                null,
                System.currentTimeMillis(),
                key,
                enhancePayload(key, value, headers));
    }

    private Object enhancePayload(Object key, Object value, Headers headers) {
        return ErrorPayload.builder().key(key).value(value).headers(getHeaderAsMapWithStringValue(headers)).build();
    }

    public Map<String, Object> getHeaderAsMapWithStringValue(Headers headers) {
        Map<String, Object> headerMap = new HashMap<>();
        if (headers != null) {
            headers.forEach(header -> headerMap.put(header.key(), new String(header.value())));
        }
        return headerMap;
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
                        !header.key().equals(MessageHeaderAttributes.ERROR_TIMESTAMP)) {
                    newHeader.add(new RecordHeader(header.key(), header.value()));
                }
            });
        }
        return headerAsMap;
    }

    @SneakyThrows
    private Headers getHeaders(Exception actualException, Headers msgHeaders, int partition, long offset, String topic) {
        RecordHeaders headers = new RecordHeaders();
        Map<String, byte[]> headerAsMap = copyHeadersAndGetAsMap(headers, msgHeaders);
        headers.add(new RecordHeader(MessageHeaderAttributes.TOPIC_NAME, topic.getBytes()));
        headers.add(new RecordHeader(MessageHeaderAttributes.EXCEPTION_CLASS, actualException.getClass().getName().getBytes()));
        headers.add(new RecordHeader(MessageHeaderAttributes.EXCEPTION_MESSAGE, actualException.getMessage().getBytes()));
        headers.add(new RecordHeader(MessageHeaderAttributes.RETRY_COUNT, getRetryCount(headerAsMap).getBytes()));
        headers.add(new RecordHeader(MessageHeaderAttributes.OFFSET, Long.toString(offset).getBytes()));
        headers.add(new RecordHeader(MessageHeaderAttributes.PARTITION, Integer.toString(partition).getBytes()));
        headers.add(new RecordHeader(MessageHeaderAttributes.ERROR_TIMESTAMP, LocalDateTime.now().toString().getBytes()));
        return headers;
    }

    public String getRetryCount(Map<String, byte[]> headerAsMap) {
        if (headerAsMap.get(MessageHeaderAttributes.RETRY_COUNT) != null) {
            return Integer.valueOf(Integer.parseInt(new String(headerAsMap.get(MessageHeaderAttributes.RETRY_COUNT))) + 1).toString();
        }
        return Integer.valueOf(1).toString();
    }
}
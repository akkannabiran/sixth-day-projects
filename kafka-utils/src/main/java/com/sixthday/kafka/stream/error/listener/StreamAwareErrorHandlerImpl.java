package com.sixthday.kafka.stream.error.listener;

import com.sixthday.kafka.stream.config.StreamProperties;
import com.sixthday.kafka.stream.config.util.BeanUtil;
import com.sixthday.kafka.stream.error.util.StreamErrorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
public class StreamAwareErrorHandlerImpl {
    private final Logger logger = LoggerFactory.getLogger(StreamAwareErrorHandlerImpl.class);

    private KafkaTemplate<Object, Object> dltKafkaTemplate;
    private StreamProperties streamProperties;

    public StreamAwareErrorHandlerImpl() {
        try {
            this.dltKafkaTemplate = BeanUtil.getBean("dlt_KafkaTemplate");
            this.streamProperties = BeanUtil.getBean("streamProperties");
        } catch (Exception e) {
            logger.warn("DLT integration disabled. {}", e.getMessage());
        }
    }

    public void handle(ConsumerRecord<byte[], byte[]> consumerRecord, Exception e) {
        if (consumerRecord.topic() != null || dltKafkaTemplate != null) {
            try {
                ProducerRecord<Object, Object> myProducerRecord = StreamErrorUtil.buildProducerRecord(streamProperties, consumerRecord, e);
                publishMessage(myProducerRecord);
            } catch (Exception ex) {
                log.error("Error handling failed! error={}", ex.getMessage());
            }
        }
    }

    public void handle(ProducerRecord<byte[], byte[]> producerRecord, Exception e) {
        if (producerRecord.topic() != null || dltKafkaTemplate != null) {
            try {
                ProducerRecord<Object, Object> myProducerRecord = StreamErrorUtil.buildProducerRecord(streamProperties, producerRecord, e);
                publishMessage(myProducerRecord);
            } catch (Exception ex) {
                log.error("Error handling failed! error={}", ex.getMessage());
            }
        }
    }

    public void handle(Object key, Object value, String sourceTopic, Exception e) {
        if (dltKafkaTemplate != null) {
            try {
                ProducerRecord<Object, Object> myProducerRecord = StreamErrorUtil.buildProducerRecord(streamProperties, sourceTopic, key, value, e);
                publishMessage(myProducerRecord);
            } catch (Exception ex) {
                log.error("Error handling failed! error={}", ex.getMessage());
            }
        }
    }

    private void publishMessage(ProducerRecord<Object, Object> producerRecord) {
        try {
            if (dltKafkaTemplate == null || producerRecord.topic() == null || producerRecord.value() == null) {
                log.info("Successfully ignored the message to topic={}, Message payload={}, headers={}", producerRecord.topic(), StreamErrorUtil.getStringFromObject(producerRecord.value()), StreamErrorUtil.getStringFromObject(StreamErrorUtil.getHeaderAsMapWithStringValue(producerRecord.headers())));
            } else {
                dltKafkaTemplate.send(producerRecord).get();
                log.info("Successfully published the message to topic={}, Message payload={}, headers={}", producerRecord.topic(), StreamErrorUtil.getStringFromObject(producerRecord.value()), StreamErrorUtil.getStringFromObject(StreamErrorUtil.getHeaderAsMapWithStringValue(producerRecord.headers())));
            }
        } catch (Exception e) {
            log.error("Unable to publish the message to topic={}, Error={}, Message payload={}, headers={}", producerRecord.topic(), e.getMessage(), StreamErrorUtil.getStringFromObject(producerRecord.value()), StreamErrorUtil.getStringFromObject(StreamErrorUtil.getHeaderAsMapWithStringValue(producerRecord.headers())));
        }
    }
}

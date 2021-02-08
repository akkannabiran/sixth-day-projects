package com.sixthday.kafka.connect.jdbc.error.handler;

import com.sixthday.kafka.connect.jdbc.config.CustomJDBCSinkConfig;
import com.sixthday.kafka.connect.jdbc.error.model.ErrorPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.sink.SinkRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ErrorRecordHandler {

    public static KafkaProducer<Object, Object> kafkaProducer;

    private final Logger LOGGER = LoggerFactory.getLogger(ErrorRecordHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final CustomJDBCSinkConfig customJdbcSinkConfig;

    public ErrorRecordHandler(CustomJDBCSinkConfig customJdbcSinkConfig) {
        this.customJdbcSinkConfig = customJdbcSinkConfig;
        if (!"".equals(customJdbcSinkConfig.dltName())) {
            customJdbcSinkConfig.producerConfig().put(ProducerConfig.CLIENT_ID_CONFIG, this.customJdbcSinkConfig.originals().get("task.id"));
            this.kafkaProducer = new KafkaProducer<>(customJdbcSinkConfig.producerConfig());
        }
    }

    @SneakyThrows
    public void handleErrorSinkRecord(SinkRecord sinkRecord, Throwable t) {
        if (kafkaProducer != null) {
            try {
                kafkaProducer.send(new ProducerRecord<>(customJdbcSinkConfig.dltName(),
                        sinkRecord.key(),
                        ErrorPayload.builder().key(sinkRecord.key()).value(sinkRecord.value()).history(history(sinkRecord, t))
                                .build())).get();
            } catch (Exception e) {
                throw new ConnectException("Unable to send message to DLT topic configured, '"
                        + customJdbcSinkConfig.dltName() + "'", e);
            }
            LOGGER.info("Message has been published to DLT topic '{}'", customJdbcSinkConfig.dltName());
        } else {
            LOGGER.warn("Enable dead letter topic using '{}' configuration.", CustomJDBCSinkConfig.ERRORS_DLT_TOPIC_NAME);
        }
        if (customJdbcSinkConfig.isErrorsLogIncludeMessage()) {
            LOGGER.error("ErrorRecord payload key='{}', sourceTopic='{}', value='{}', exception='{}'",
                    objectMapper.writeValueAsString(sinkRecord.key()), sinkRecord.topic(),
                    objectMapper.writeValueAsString(sinkRecord.value()), t.getMessage());
        }
    }

    private Object history(SinkRecord sinkRecord, Throwable t) {
        Map<String, Object> history = new HashMap<>();
        if (customJdbcSinkConfig.isErrorsEnableDLTHeaders()) {
            history.put("topic_name", sinkRecord.topic());
            history.put("offset", sinkRecord.kafkaOffset());
            history.put("partition", sinkRecord.kafkaPartition());
            history.put("timestamp", LocalDateTime.now().toString());
            history.put("exception_message", t.getMessage());
            history.put("exception_class", t.getClass());
            history.put("root_cause_exception_class", t.getCause() != null ? t.getCause().getClass() : null);
            history.put("root_cause_exception_message", t.getCause() != null ? t.getCause().getMessage() : null);
        }
        return history;
    }
}

package com.sixthday.kafka.consumer.error.listener;

import com.sixthday.kafka.consumer.error.exception.RecoverableMessageException;
import com.sixthday.kafka.consumer.error.exception.UnrecoverableMessageException;
import com.sixthday.kafka.consumer.error.util.ConsumerErrorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerAwareErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;

@Slf4j
public class ConsumerAwareErrorHandlerImpl implements ConsumerAwareErrorHandler {

    private final KafkaTemplate<String, Object> retryKafkaTemplate;
    private final KafkaTemplate<String, Object> dlKafkaTemplate;
    private final String retryTopicName;
    private final String deadLetterTopicName;

    public ConsumerAwareErrorHandlerImpl(KafkaTemplate<String, Object> retryKafkaTemplate, KafkaTemplate<String, Object> dlqKafkaTemplate, String retryTopicName, String deadLetterTopicName) {
        this.retryKafkaTemplate = retryKafkaTemplate;
        this.dlKafkaTemplate = dlqKafkaTemplate;
        this.retryTopicName = retryTopicName;
        this.deadLetterTopicName = deadLetterTopicName;
    }

    @Override
    public void handle(Exception e, ConsumerRecord<?, ?> consumerRecord, Consumer<?, ?> consumer) {

        Throwable rootCause = NestedExceptionUtils.getRootCause(e);

        try {
            if (e.getCause() != null && e.getCause() instanceof DeserializationException) {
                handleDeserializationException(e, (DeserializationException) e.getCause(), consumerRecord);
            } else if (rootCause instanceof RecoverableMessageException) {
                handleRecoverableException(e, (RecoverableMessageException) rootCause, consumerRecord);
            } else if (rootCause instanceof UnrecoverableMessageException) {
                handleUnRecoverableException(e, (UnrecoverableMessageException) rootCause, consumerRecord);
            } else {
                handleUnknownException(e, (rootCause != null ? rootCause : e), consumerRecord);
            }
        } catch (Exception ex) {
            if (consumerRecord != null) {
                log.info("ConsumerRecord key={}, value={}, headers={}", ConsumerErrorUtil.getStringFromObject(consumerRecord.key())
                        , ConsumerErrorUtil.getStringFromObject(consumerRecord.value()), ConsumerErrorUtil.getHeaderAsMapWithStringValue(consumerRecord.headers()));
            }
            log.error("Exception caught! Unable to handle the error", ex);
        } finally {
            consumer.commitSync();
        }
    }

    private void handleDeserializationException(Exception actualException, DeserializationException e, ConsumerRecord<?, ?> consumerRecord) {
        if (consumerRecord != null) {
            if (ConsumerErrorUtil.messageDispatch(deadLetterTopicName, true)) {
                ProducerRecord<String, Object> producerRecord = ConsumerErrorUtil.buildProducerRecord(actualException, e, e, consumerRecord, deadLetterTopicName, true);
                publishMessage(producerRecord, dlKafkaTemplate);
            } else {
                log.info("ConsumerRecord key={}, value={}, headers={} DeserializationRecord isKey={}, data={}, headers={}", ConsumerErrorUtil.getStringFromObject(consumerRecord.key())
                        , ConsumerErrorUtil.getStringFromObject(consumerRecord.value()), ConsumerErrorUtil.getHeaderAsMapWithStringValue(consumerRecord.headers()),
                        e.isKey(), ConsumerErrorUtil.getStringFromByteArray(e.getData()), ConsumerErrorUtil.getHeaderAsMapWithStringValue(e.getHeaders()));
            }
            log.info("{} caught & handled successfully", e.getClass().getSimpleName());
        } else {
            log.info("{} caught with empty ConsumerRecord", e.getClass().getSimpleName());
        }
    }

    private void handleRecoverableException(Exception actualException, RecoverableMessageException e, ConsumerRecord<?, ?> consumerRecord) {
        if (retryKafkaTemplate != null) {
            handleException(actualException, e, e.getActualCause(), retryKafkaTemplate, consumerRecord, e.getTopicName() != null ? e.getTopicName() : retryTopicName, e.isPublishMessageToTopic(), false);
        } else {
            log.info("Retry topic is not configured! Trying to publish the message to dead letter topic");
            handleException(actualException, e, e.getActualCause(), dlKafkaTemplate, consumerRecord, e.getTopicName() != null ? e.getTopicName() : deadLetterTopicName, e.isPublishMessageToTopic(), true);
        }
    }

    private void handleUnRecoverableException(Exception actualException, UnrecoverableMessageException e, ConsumerRecord<?, ?> consumerRecord) {
        handleException(actualException, e, e.getActualCause(), dlKafkaTemplate, consumerRecord, e.getTopicName() != null ? e.getTopicName() : deadLetterTopicName, e.isPublishMessageToTopic(), true);
    }

    private void handleUnknownException(Exception actualException, Throwable e, ConsumerRecord<?, ?> consumerRecord) {
        handleException(actualException, e, e, dlKafkaTemplate, consumerRecord, deadLetterTopicName, true, true);
    }

    private void handleException(Exception actualException, Throwable cause, Throwable rootCause, KafkaTemplate<String, Object> kafkaTemplate, ConsumerRecord<?, ?> consumerRecord, String targetTopic, boolean publishToTopic, boolean enhancePayload) {
        if (consumerRecord != null) {
            if (ConsumerErrorUtil.messageDispatch(targetTopic, publishToTopic)) {
                ProducerRecord<String, Object> producerRecord = ConsumerErrorUtil.buildProducerRecord(actualException, cause, rootCause, consumerRecord, targetTopic, enhancePayload);
                publishMessage(producerRecord, kafkaTemplate);
            } else {
                log.info("Message dispatch is disabled! ConsumerRecord key={}, value={}, headers={}", ConsumerErrorUtil.getStringFromObject(consumerRecord.key())
                        , ConsumerErrorUtil.getStringFromObject(consumerRecord.value()), ConsumerErrorUtil.getStringFromObject(ConsumerErrorUtil.getHeaderAsMapWithStringValue(consumerRecord.headers())));
            }
            log.info("{} caught & handled successfully", rootCause.getClass().getSimpleName());
        } else {
            log.info("{} caught with empty ConsumerRecords", rootCause.getClass().getSimpleName());
        }
    }

    private void publishMessage(ProducerRecord<String, Object> producerRecord, KafkaTemplate<String, Object> kafkaTemplate) {
        try {
            if (kafkaTemplate == null || producerRecord.topic() == null || producerRecord.value() == null) {
                log.info("Successfully ignored the message to topic={}, Message payload={}, headers={}", producerRecord.topic(), ConsumerErrorUtil.getStringFromObject(producerRecord.value()), ConsumerErrorUtil.getStringFromObject(ConsumerErrorUtil.getHeaderAsMapWithStringValue(producerRecord.headers())));
            } else {
                kafkaTemplate.send(producerRecord).get();
                log.info("Successfully published the message to topic={}, Message payload={}, headers={}", producerRecord.topic(), ConsumerErrorUtil.getStringFromObject(producerRecord.value()), ConsumerErrorUtil.getStringFromObject(ConsumerErrorUtil.getHeaderAsMapWithStringValue(producerRecord.headers())));
            }
        } catch (Exception e) {
            log.info("Unable to publish the message to topic={}, Message payload={}, headers={}", producerRecord.topic(), ConsumerErrorUtil.getStringFromObject(producerRecord.value()), ConsumerErrorUtil.getStringFromObject(ConsumerErrorUtil.getHeaderAsMapWithStringValue(producerRecord.headers())));
        }
    }
}
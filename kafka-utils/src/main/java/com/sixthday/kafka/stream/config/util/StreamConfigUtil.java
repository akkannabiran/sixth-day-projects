package com.sixthday.kafka.stream.config.util;

import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@UtilityClass
public class StreamConfigUtil {
    private final Logger logger = LoggerFactory.getLogger(StreamConfigUtil.class);

    private final String DLT_PRODUCER_FACTORY_BEAN_ID = "_ProducerFactory";
    private final String DLT_KAFKA_TEMPLATE_BEAN_ID = "_KafkaTemplate";

    @SuppressWarnings("unchecked")
    public static KafkaTemplate<String, Object> buildKafkaTemplate(GenericWebApplicationContext genericWebApplicationContext, Properties properties, String topicName, boolean isDlt, String key) {
        if (isDlt) {
            try {
                logger.info("Trying {} bean from application context", key + DLT_KAFKA_TEMPLATE_BEAN_ID);
                return (KafkaTemplate<String, Object>) genericWebApplicationContext.getBean(key +  DLT_KAFKA_TEMPLATE_BEAN_ID);
            } catch (BeansException var5) {
                logger.info("{} bean is not available, creating one", key +  DLT_KAFKA_TEMPLATE_BEAN_ID);
            }
        }
        KafkaTemplate<String, Object> kafkaTemplate = topicName
                != null ? new KafkaTemplate<>(buildProducerFactory(genericWebApplicationContext, properties, isDlt, key)) : null;
        if (isDlt && kafkaTemplate != null) {
            genericWebApplicationContext.registerBean(key +  DLT_KAFKA_TEMPLATE_BEAN_ID, KafkaTemplate.class, () -> kafkaTemplate);
        }
        return kafkaTemplate;
    }

    @SuppressWarnings("unchecked")
    public static ProducerFactory<String, Object> buildProducerFactory(GenericWebApplicationContext genericWebApplicationContext, Properties properties, boolean isDlt, String key) {
        if (isDlt) {
            try {
                logger.info("Trying {} bean from application context", key +  DLT_PRODUCER_FACTORY_BEAN_ID);
                return (ProducerFactory<String, Object>) genericWebApplicationContext.getBean(key + "_" + DLT_PRODUCER_FACTORY_BEAN_ID);
            } catch (BeansException e) {
                logger.info("{} bean is not available, creating one", key +  DLT_PRODUCER_FACTORY_BEAN_ID);
            }
        }
        ProducerFactory<String, Object> producerFactory = new DefaultKafkaProducerFactory<>(covertPropertiesToMap(properties));
        if (isDlt) {
            genericWebApplicationContext.registerBean(key +  DLT_PRODUCER_FACTORY_BEAN_ID, ProducerFactory.class, () -> producerFactory);
        }
        return producerFactory;
    }

    public Map<String, Object> covertPropertiesToMap(Properties inputProp) {
        Map<String, Object> outputMap = new HashMap<>();
        inputProp.forEach((key, value) -> outputMap.put((String) key, value));
        return outputMap;
    }
}

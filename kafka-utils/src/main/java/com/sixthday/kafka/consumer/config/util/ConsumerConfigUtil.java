package com.sixthday.kafka.consumer.config.util;

import com.sixthday.kafka.consumer.config.ConsumerProperties;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.kafka.core.*;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@UtilityClass
public class ConsumerConfigUtil {

    private final Logger logger = LoggerFactory.getLogger(ConsumerConfigUtil.class);

    private final String DLT_PRODUCER_FACTORY_BEAN_ID = "dlt_ProducerFactory";
    private final String DLT_KAFKA_TEMPLATE_BEAN_ID = "dlt_KafkaTemplate";

    public String getTopicName(ConsumerProperties.ListenerInfo listenerInfo) {
        return listenerInfo != null ? listenerInfo.getTopic() : null;
    }

    public ConsumerFactory<String, Object> buildConsumerFactory(Properties consumerProp, Properties listenerProp) {
        return new DefaultKafkaConsumerFactory<>(bindConfig(consumerProp, listenerProp));
    }

    @SuppressWarnings("unchecked")
    public ProducerFactory<String, Object> buildProducerFactory(GenericWebApplicationContext genericWebApplicationContext, Properties properties, ConsumerProperties.ListenerInfo listenerInfo, boolean isDlt) {
        if (isDlt) {
            try {
                logger.info("Trying {} bean from application context", DLT_PRODUCER_FACTORY_BEAN_ID);
                return (ProducerFactory<String, Object>) genericWebApplicationContext.getBean(DLT_PRODUCER_FACTORY_BEAN_ID);
            } catch (BeansException e) {
                logger.info("{} bean is not available, creating one", DLT_PRODUCER_FACTORY_BEAN_ID);
            }
        }
        ProducerFactory<String, Object> producerFactory = new DefaultKafkaProducerFactory<>(bindConfig(properties, listenerInfo.getProperties()));
        if (isDlt) {
            genericWebApplicationContext.registerBean(DLT_PRODUCER_FACTORY_BEAN_ID, ProducerFactory.class, () -> producerFactory);
        }
        return producerFactory;
    }

    @SuppressWarnings("unchecked")
    public KafkaTemplate<String, Object> buildKafkaTemplate(GenericWebApplicationContext genericWebApplicationContext, Properties properties, ConsumerProperties.ListenerInfo listenerInfo, boolean isDlt) {
        if (isDlt) {
            try {
                logger.info("Trying {} bean from application context", DLT_KAFKA_TEMPLATE_BEAN_ID);
                return (KafkaTemplate<String, Object>) genericWebApplicationContext.getBean(DLT_KAFKA_TEMPLATE_BEAN_ID);
            } catch (BeansException var5) {
                logger.info("{} bean is not available, creating one", DLT_KAFKA_TEMPLATE_BEAN_ID);
            }
        }
        KafkaTemplate<String, Object> kafkaTemplate = listenerInfo != null ? new KafkaTemplate<>(buildProducerFactory(genericWebApplicationContext, properties, listenerInfo, isDlt)) : null;
        if (isDlt && kafkaTemplate != null) {
            genericWebApplicationContext.registerBean(DLT_KAFKA_TEMPLATE_BEAN_ID, KafkaTemplate.class, () -> kafkaTemplate);
        }
        return kafkaTemplate;
    }

    public Map<String, Object> bindConfig(Properties consumerProp, Properties listenerProp) {
        Map<String, Object> config = new HashMap<>();
        if (!CollectionUtils.isEmpty(consumerProp))
            config.putAll(covertPropertiesToMap(consumerProp));
        if (!CollectionUtils.isEmpty(listenerProp))
            config.putAll(covertPropertiesToMap(listenerProp));
        return config;
    }

    public Map<String, Object> covertPropertiesToMap(Properties inputProp) {
        Map<String, Object> outputMap = new HashMap<>();
        inputProp.forEach((key, value) -> outputMap.put((String) key, value));
        return outputMap;
    }
}

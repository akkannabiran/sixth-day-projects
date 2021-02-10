package com.sixthday.kafka.stream.config;

import com.sixthday.kafka.stream.config.util.StreamConfigUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.GenericWebApplicationContext;

@Component
public class StreamConfig {

    private final StreamProperties streamProperties;
    private final GenericWebApplicationContext genericWebApplicationContext;

    @Autowired
    public StreamConfig(StreamProperties streamProperties, GenericWebApplicationContext genericWebApplicationContext) {
        this.streamProperties = streamProperties;
        this.genericWebApplicationContext = genericWebApplicationContext;
    }

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kafkaStreamsConfiguration() {
        StreamConfigUtil.buildKafkaTemplate(genericWebApplicationContext,
                streamProperties.getKafka().getProducer().getProperties(),
                streamProperties.getKafka().getStream().getApps().entrySet().iterator().next().getValue().getDlTopic(),
                true, "dlt");
        return new KafkaStreamsConfiguration(
                StreamConfigUtil.covertPropertiesToMap(streamProperties.getKafka().getStream().getProperties()));
    }
}
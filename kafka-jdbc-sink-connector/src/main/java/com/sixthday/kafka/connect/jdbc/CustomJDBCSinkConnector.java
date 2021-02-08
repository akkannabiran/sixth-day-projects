package com.sixthday.kafka.connect.jdbc;

import com.sixthday.kafka.connect.jdbc.config.CustomJDBCSinkConfig;
import com.sixthday.kafka.connect.jdbc.task.CustomJDBCSinkTask;
import com.sixthday.kafka.connect.jdbc.util.Version;
import org.apache.kafka.common.config.Config;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CustomJDBCSinkConnector extends SinkConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomJDBCSinkConnector.class);
    private Map<String, String> configurations;

    @Override
    public void start(Map<String, String> configurations) {
        this.configurations = configurations;
    }

    @Override
    public Class<? extends Task> taskClass() {
        return CustomJDBCSinkTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int tasks) {
        final List<Map<String, String>> configurations =
                IntStream.range(0, tasks)
                        .mapToObj(i -> this.configurations).collect(Collectors.toList());
        LOGGER.info("Setting up task(s) configurations for {} worker(s)", tasks);
        return configurations;
    }

    @Override
    public void stop() {
    }

    @Override
    public String version() {
        return Version.getVersion();
    }

    @Override
    public Config validate(Map<String, String> connectorConfigs) {
        return super.validate(connectorConfigs);
    }

    @Override
    public ConfigDef config() {
        return CustomJDBCSinkConfig.configDef();
    }
}

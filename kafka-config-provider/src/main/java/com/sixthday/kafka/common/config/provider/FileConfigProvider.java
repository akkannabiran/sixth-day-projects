package com.sixthday.kafka.common.config.provider;

import org.apache.kafka.common.config.ConfigData;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.config.provider.ConfigProvider;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class FileConfigProvider implements ConfigProvider {

    @Override
    public ConfigData get(String absoluteFilePath) {
        return new ConfigData(createMapFromPropertyFile(absoluteFilePath, new HashSet<>()));
    }

    @Override
    public ConfigData get(String absoluteFilePath, Set<String> keys) {
        return new ConfigData(createMapFromPropertyFile(absoluteFilePath, keys));
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> map) {
    }

    private Map<String, String> createMapFromPropertyFile(String absoluteFilePath, Set<String> keys) {
        Map<String, String> configMap = new HashMap<>();
        if (absoluteFilePath != null && keys != null) {
            try (FileInputStream fileInputStream = new FileInputStream(new File(absoluteFilePath))) {
                Properties properties = new Properties();
                properties.load(fileInputStream);
                for (String key : (keys.isEmpty() ? properties.stringPropertyNames() : keys)) {
                    configMap.put(key, properties.getProperty(key));
                }
            } catch (Exception e) {
                throw new ConfigException("Exception caught while reading from file " + absoluteFilePath);
            }
        }
        return configMap;
    }
}

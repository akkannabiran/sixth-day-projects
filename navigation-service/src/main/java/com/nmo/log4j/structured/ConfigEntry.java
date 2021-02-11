package com.sixthday.log4j.structured;

import lombok.Getter;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.*;

@Plugin(name = "Key", category = Node.CATEGORY)
@Getter
public class ConfigEntry {

    private final String key;
    private final String value;

    public ConfigEntry(String key, String value) {

        this.key = key;
        this.value = value;
    }

    @PluginFactory
    public static ConfigEntry create(@PluginAttribute(value = "name") String key,
                                     @PluginValue(value = "value") String pattern
    ) {
        return new ConfigEntry(key, pattern);
    }
}

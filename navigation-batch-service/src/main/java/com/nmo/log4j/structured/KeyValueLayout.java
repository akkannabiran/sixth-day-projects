package com.sixthday.log4j.structured;

import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

@Plugin(name = "KeyValueLayout", category = Node.CATEGORY, elementType = Layout.ELEMENT_TYPE, printObject = false)
public class KeyValueLayout extends AbstractStringLayout {

    private final List<Entry> entries;

    public KeyValueLayout(Charset charset, ConfigEntry... entries) {
        super(charset);
        this.entries = Arrays.stream(entries).map(e -> toEntry(e, charset)).collect(Collectors.toList());
    }

    @PluginFactory
    public static KeyValueLayout createLayout(@PluginAttribute(value = "charset", defaultString = "UTF-8") Charset charset,
                                              @PluginElement(value = "Key") ConfigEntry[] entries
    ) {
        return new KeyValueLayout(charset, entries);
    }

    private static Entry toEntry(ConfigEntry configEntry, Charset charset) {
        return new Entry(
                PatternLayout.newBuilder().withCharset(charset).withPattern(configEntry.getKey()).build(),
                PatternLayout.newBuilder().withCharset(charset).withPattern(configEntry.getValue()).build()
        );
    }

    @Override
    public String toSerializable(LogEvent event) {
        String[] formattedEntries = entries.stream()
                .map(e -> e.toSerializable(event))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toArray(String[]::new);
        return String.join(", ", formattedEntries);
    }

    public int size() {
        return entries.size();
    }

    private static class Entry {
        private final PatternLayout key;
        private final PatternLayout value;

        public Entry(PatternLayout key, PatternLayout value) {
            this.key = key;
            this.value = value;
        }

        public Optional<String> toSerializable(LogEvent event) {
            String formattedKey = key.toSerializable(event);
            String formattedValue = value.toSerializable(event);

            if (formattedKey.isEmpty() || formattedValue.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(String.format("%s=\"%s\"", formattedKey, formattedValue));
            }
        }
    }
}

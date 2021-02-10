package com.sixthday.kafka.common.config.provider;

import org.apache.kafka.common.config.ConfigData;
import org.apache.kafka.common.config.ConfigException;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FileConfigProviderTest {

    @Test
    public void testGetConfigFromFile() {
        FileConfigProvider fileConfigProvider = new FileConfigProvider();

        ConfigData configData = fileConfigProvider.get(Paths.get("src", "test", "resources", "application.properties").toAbsolutePath().toString());

        assertNotNull(configData);
        assertEquals(5, configData.data().size());
        assertEquals("value1", configData.data().get("property1"));
        assertEquals("value2", configData.data().get("property2"));
        assertEquals("value3", configData.data().get("property3"));
        assertEquals("value4", configData.data().get("property4"));
        assertEquals("value5", configData.data().get("property5"));
    }

    @Test
    public void testGetConfigFromFileAndKeySets() {
        FileConfigProvider fileConfigProvider = new FileConfigProvider();

        ConfigData configData = fileConfigProvider.get(Paths.get("src", "test", "resources", "application.properties").toAbsolutePath().toString(),
                new HashSet<>(Arrays.asList("property1", "property2", "property3", "property4", "property5")));

        assertNotNull(configData);
        assertEquals(5, configData.data().size());
        assertEquals("value1", configData.data().get("property1"));
        assertEquals("value2", configData.data().get("property2"));
        assertEquals("value3", configData.data().get("property3"));
        assertEquals("value4", configData.data().get("property4"));
        assertEquals("value5", configData.data().get("property5"));
    }

    @Test
    public void testGetConfigFromFileAndEmptyKeySets() {
        FileConfigProvider fileConfigProvider = new FileConfigProvider();

        ConfigData configData = fileConfigProvider.get(Paths.get("src", "test", "resources", "application.properties").toAbsolutePath().toString(),
                new HashSet<>());

        assertNotNull(configData);
        assertEquals(5, configData.data().size());
        assertEquals("value1", configData.data().get("property1"));
        assertEquals("value2", configData.data().get("property2"));
        assertEquals("value3", configData.data().get("property3"));
        assertEquals("value4", configData.data().get("property4"));
        assertEquals("value5", configData.data().get("property5"));
    }

    @Test
    public void testGetConfigFromFileAndNullKeySets() {
        FileConfigProvider fileConfigProvider = new FileConfigProvider();

        ConfigData configData = fileConfigProvider.get(Paths.get("src", "test", "resources", "application.properties").toAbsolutePath().toString(),
                null);

        assertNotNull(configData);
        assertEquals(0, configData.data().size());
    }

    @Test(expected = ConfigException.class)
    public void testGetConfigFromNonExistenceFile() {
        FileConfigProvider fileConfigProvider = new FileConfigProvider();

        ConfigData configData = fileConfigProvider.get(Paths.get("src", "test", "resources", "application.txt").toAbsolutePath().toString(),
                new HashSet<>());

        assertNotNull(configData);
        assertEquals(0, configData.data().size());
    }
}
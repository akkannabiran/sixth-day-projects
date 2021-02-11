package com.sixthday.log4j.structured;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.StringFormattedMessage;
import org.junit.*;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.MDC;

import java.nio.charset.Charset;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(PowerMockRunner.class)
public class KeyValueLayoutTest {

    @Before
    public void before() {
        LogManager.shutdown();
    }

    @After
    public void after() {
        LogManager.shutdown();
    }

    @Test
    public void constantKeyValue() {
        KeyValueLayout layout = new KeyValueLayout(
                Charset.defaultCharset(),
                new ConfigEntry("foo", "bar")
        );
        Log4jLogEvent logEvent = new Log4jLogEvent();
        String output = layout.toSerializable(logEvent);
        assertThat(output, is(equalTo("foo=\"bar\"")));
    }

    @Test
    public void patternKeyValue() {
        KeyValueLayout layout = new KeyValueLayout(
                Charset.defaultCharset(),
                new ConfigEntry("%level", "%message")
        );
        Log4jLogEvent logEvent = Log4jLogEvent.newBuilder().setLevel(Level.INFO).setMessage(new StringFormattedMessage("my message")).build();
        String output = layout.toSerializable(logEvent);
        assertThat(output, is(equalTo("INFO=\"my message\"")));
    }

    @Test
    public void multipleEntries() {
        KeyValueLayout layout = new KeyValueLayout(
                Charset.defaultCharset(),
                new ConfigEntry("key1", "value1"),
                new ConfigEntry("key2", "value2")
        );
        Log4jLogEvent logEvent = Log4jLogEvent.newBuilder().setMessage(new StringFormattedMessage("my message")).build();
        String output = layout.toSerializable(logEvent);
        assertThat(output, is(equalTo("key1=\"value1\", key2=\"value2\"")));
    }

    @Test
    public void mdc() {
        MDC.clear();
        try {
            MDC.put("mdc_key", "mdc_value");
            KeyValueLayout layout = new KeyValueLayout(
                    Charset.defaultCharset(),
                    new ConfigEntry("mdc_entry", "%X{mdc_key}")
            );
            Log4jLogEvent logEvent = Log4jLogEvent.newBuilder().build();
            String output = layout.toSerializable(logEvent);
            assertThat(output, is(equalTo("mdc_entry=\"mdc_value\"")));
        } finally {
            MDC.clear();
        }
    }

    @Test
    public void ignoreEmpty() {
        KeyValueLayout layout = new KeyValueLayout(
                Charset.defaultCharset(),
                new ConfigEntry("key", "value"),
                new ConfigEntry("message", "%message"),
                new ConfigEntry("%message", "message")
        );
        Log4jLogEvent logEvent = Log4jLogEvent.newBuilder().build();
        String output = layout.toSerializable(logEvent);
        assertThat(output, is(equalTo("key=\"value\"")));
    }
}

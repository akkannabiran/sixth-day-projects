package com.sixthday.logger.logging;

import lombok.SneakyThrows;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertTrue;

public class LoggingEventTest {
    @Test
    @SneakyThrows
    public void testConstructorIsPrivate() {
        Constructor<LoggingEvent> constructor = LoggingEvent.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
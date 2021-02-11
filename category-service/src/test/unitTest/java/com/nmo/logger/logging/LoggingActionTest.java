package com.sixthday.logger.logging;

import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class LoggingActionTest {

    @Test
    @SneakyThrows
    public void testConstructorIsPrivate() {
        Constructor<LoggingAction> constructor = LoggingAction.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
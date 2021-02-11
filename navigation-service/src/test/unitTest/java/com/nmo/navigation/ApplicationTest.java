package com.sixthday.navigation;

import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static junit.framework.TestCase.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationTest {
    @Test
    @SneakyThrows
    public void testConstructorIsProtected() {
        Constructor<Application> constructor = Application.class.getDeclaredConstructor();
        assertTrue(Modifier.isProtected(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
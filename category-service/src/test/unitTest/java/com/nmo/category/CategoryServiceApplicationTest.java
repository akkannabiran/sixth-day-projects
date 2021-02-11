package com.sixthday.category;

import lombok.SneakyThrows;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static junit.framework.TestCase.assertTrue;

public class CategoryServiceApplicationTest {


    @Test
    public void contextLoads() {
    }

    @Test
    @SneakyThrows
    public void testConstructorIsPrivate() {
        Constructor<CategoryServiceApplication> constructor = CategoryServiceApplication.class.getDeclaredConstructor();
        assertTrue(Modifier.isProtected(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
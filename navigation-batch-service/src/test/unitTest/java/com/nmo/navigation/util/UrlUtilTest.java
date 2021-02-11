package com.sixthday.navigation.util;

import lombok.SneakyThrows;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class UrlUtilTest {

    @Test
    public void shouldNotAppendParameterWhenUrlIsEmpty() {
        assertEquals("", UrlUtil.appendQueryParameterToUrl("#", "", "navpath", "thisNavPath"));
    }

    @Test
    public void shouldNotAppendParameterWhenIdIsHash() {
        assertEquals("http://url", UrlUtil.appendQueryParameterToUrl("#", "http://url", "navpath", "thisNavPath"));
    }

    @Test
    public void shouldAppendParameterAsFirstParameterIfNoneExists() {
        assertEquals("http://url?navpath=thisNavPath", UrlUtil.appendQueryParameterToUrl("A", "http://url", "navpath", "thisNavPath"));
    }

    @Test
    public void shouldAppendParameterAsSecondParameterIfItAlreadyHasParameters() {
        assertEquals("http://url?one=firstParameter&navpath=thisNavPath", UrlUtil.appendQueryParameterToUrl("A", "http://url?one=firstParameter", "navpath", "thisNavPath"));
    }

    @Test
    public void shouldAppendParameterAsFirstParameterIfItEndsWithQuestionMark() {
        assertEquals("http://url?navpath=thisNavPath", UrlUtil.appendQueryParameterToUrl("A", "http://url?", "navpath", "thisNavPath"));
    }

    @Test
    public void shouldRetrieveLastCategoryIdWhenPathContains2CategoryIds() {
        assertThat(UrlUtil.getLastCategoryId("cat1_cat2").orElse(null), is("cat2"));
    }

    @Test
    public void shouldReturnLastCategoryIdWhenPathContains2CategoryIds() {
        assertThat(UrlUtil.getLastCategoryId("cat1").orElse(null), is("cat1"));
    }

    @Test
    public void shouldReturnEmptyOptionalWhenPathIsEmpty() {
        assertNull(UrlUtil.getLastCategoryId("").orElse(null));
    }

    @Test
    public void shouldReturnEmptyOptionalWhenPathIsNull() {
        assertNull(UrlUtil.getLastCategoryId(null).orElse(null));
    }

    @Test
    public void shouldReturnEmptyOptionalWhenPathEndsWithUnderscore() {
        assertNull(UrlUtil.getLastCategoryId("cat1_").orElse(null));
    }

    @Test
    @SneakyThrows
    public void testConstructorIsPrivate() {
        Constructor<UrlUtil> constructor = UrlUtil.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
package com.sixthday;

import lombok.SneakyThrows;
import org.jboss.logging.MDC;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static com.sixthday.sixthdayLogging.EventType.DTMESSAGE;
import static com.sixthday.sixthdayLogging.OperationType.ELASTICSEARCH_UPSERT_LEFTNAV_DOCUMENT;
import static com.sixthday.sixthdayLogging.logOperation;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class sixthdayLoggingTest {

    private Logger logger;

    @Before
    public void setUp() {
        logger = LoggerFactory.getLogger(sixthdayLoggingTest.class);
    }

    @Test
    public void shouldCleanUpMDC() {
        MDC.clear();
        MDC.put("my_test_data", "my_test_value");
        logOperation(logger, DTMESSAGE, ELASTICSEARCH_UPSERT_LEFTNAV_DOCUMENT, () -> {
            assertThat("MDC was not used; is this test still needed?", MDC.getMap().size(), greaterThan(1));
            return null;
        });

        assertThat("Data was leftover in MDC", MDC.getMap().entrySet(), hasSize(1));
    }

    @Test
    public void shouldReturnValueFromRequestedOperation() {
        String expected = "expected value";
        String actual = logOperation(logger, DTMESSAGE, ELASTICSEARCH_UPSERT_LEFTNAV_DOCUMENT, () -> expected);

        assertThat(actual, is(expected));
    }

    @Test
    @SneakyThrows
    public void testConstructorIsPrivate() {
        Constructor<sixthdayLogging> constructor = sixthdayLogging.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void testLogErrorWithThreeParams() {
        sixthdayLogging.logError(logger, DTMESSAGE, ELASTICSEARCH_UPSERT_LEFTNAV_DOCUMENT);
    }

    @Test
    public void testLogErrorWithFourParams() {
        sixthdayLogging.logError(logger, DTMESSAGE, ELASTICSEARCH_UPSERT_LEFTNAV_DOCUMENT, "");
    }
}
package com.sixthday.logger.logging;

import com.sixthday.logger.logging.setup.ClassWithLoggableEvent;
import com.sixthday.logger.logging.setup.LoggingTestConfig;
import com.sixthday.logger.util.LogCapture;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LoggingTestConfig.class, properties = {"spring.cloud.consul.enabled=false"})
public class LoggableEventTest {

    @Autowired
    private ClassWithLoggableEvent classWithLoggableEvent;

    private ByteArrayOutputStream loggingOutput;

    @BeforeClass
    public static void setLoggerContextSelector() {
        System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.selector.BasicContextSelector");
    }

    @Before
    public void setUp() {
        loggingOutput = LogCapture.captureLogOutput(LogEvent.class);
    }

    @After
    public void tearDown() {
        LogCapture.stopLogCapture(LogEvent.class, loggingOutput);
    }

    @Test
    public void shouldLogSuccessfulEvent() {
        classWithLoggableEvent.logSuccessFulEvent("DATA");

        String actualLog = new String(loggingOutput.toByteArray());
        assertThat(actualLog).contains("event_type=\"EVENT_TYPE\", action=\"ACTION\", status=\"Success\", duration_millis=\"");
    }

    @Test
    public void shouldLogFailedEvent() {

        try {
            classWithLoggableEvent.logFailedEvent("DATA");
        } catch (Exception ignore) {
        }

        String actualLog = new String(loggingOutput.toByteArray());
        assertThat(actualLog).contains("event_type=\"EVENT_TYPE\", action=\"ACTION\", status=\"Failed\", ClassName=\"Exception\", msg=\"Failed event\", duration_millis=\"");
    }

    @Test
    public void shouldLogFailedEventIfStatusCodeReturnedIsServerError() throws Exception {

        classWithLoggableEvent.serverErrorStatusCode("DATA");

        String actualLog = new String(loggingOutput.toByteArray());
        assertThat(actualLog).contains("event_type=\"EVENT_TYPE\", action=\"ACTION\", status=\"Failed\", duration_millis=\"");
    }

    @Test
    public void shouldLogFailedEventIfStatusCodeReturnedIsClientError() throws Exception {

        classWithLoggableEvent.clientErrorStatusCode("DATA");

        String actualLog = new String(loggingOutput.toByteArray());
        assertThat(actualLog).contains("event_type=\"EVENT_TYPE\", action=\"ACTION\", status=\"Failed\", duration_millis=\"");
    }

    @Test
    public void shouldLogSuccessEventIfStatusCodeReturnedIsSuccess() throws Exception {

        classWithLoggableEvent.successStatusCode("DATA");

        String actualLog = new String(loggingOutput.toByteArray());
        assertThat(actualLog).contains("event_type=\"EVENT_TYPE\", action=\"ACTION\", status=\"Success\", duration_millis=\"");
    }
}

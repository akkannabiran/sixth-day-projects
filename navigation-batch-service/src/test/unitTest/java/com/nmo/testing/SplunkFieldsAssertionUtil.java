package com.sixthday.testing;

import java.io.*;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class SplunkFieldsAssertionUtil {
    private SplunkFieldsAssertionUtil() {
    }

    public static void verifySplunkFieldsOnLog(String actualLog, String[] expected) {
        verifySplunkFieldsOnLog(actualLog, "sixthdayLogType=\"ContentSyncDashboard\"", expected);
    }

    public static void verifySplunkFieldsOnLog(String actualLog, String filter, String[] expected) {
        try (BufferedReader reader = new BufferedReader(new StringReader(actualLog))) {
            boolean[] assertionExecuted = {false};
            reader.lines().filter(line -> line.contains(filter)).forEach(logLine -> {
                Stream.of(expected).forEach(str -> {
                    assertThat(logLine, containsString(str));
                });
                assertionExecuted[0] = true;
            });
            assertThat("Splunk dashboard log is expected. " + filter + " is expected in log line", assertionExecuted[0], is(Boolean.TRUE));
        } catch (IOException e) {
            fail("Input log is not ready or not availble. Error=" + e.getMessage());
        }
    }
}

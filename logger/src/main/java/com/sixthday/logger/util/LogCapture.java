package com.sixthday.logger.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.OutputStreamAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import static org.apache.logging.log4j.LogManager.getLogger;

public class LogCapture {

    public static ByteArrayOutputStream captureLogOutput(Class<?> classToLog) {
        Logger logger = (Logger) getLogger(classToLog);
        logger.setLevel(Level.ALL);

        LogCaptureOutputStream loggingOutput = new LogCaptureOutputStream();
        OutputStreamAppender appender = getBasicAppender(loggingOutput);
        loggingOutput.setAppender(appender);
        logger.addAppender(appender);

        return loggingOutput;
    }

    private static OutputStreamAppender getBasicAppender(ByteArrayOutputStream loggingOutput) {
        PatternLayout patternLayout = getBasicPatternLayout();
        OutputStreamAppender appender = OutputStreamAppender.newBuilder().setName(UUID.randomUUID().toString()).setTarget(loggingOutput).setLayout(patternLayout).build();
        appender.start();
        return appender;
    }

    private static PatternLayout getBasicPatternLayout() {
        PatternLayout patternLayout = PatternLayout.newBuilder().withPattern("%msg %X %n").build();
        return patternLayout;
    }

    public static void stopLogCapture(Class<?> classToLog, ByteArrayOutputStream loggingOutput) {
        Logger logger = (Logger) getLogger(classToLog);

        logger.removeAppender(((LogCaptureOutputStream) loggingOutput).getAppender());
    }

    private static class LogCaptureOutputStream extends ByteArrayOutputStream {

        private Appender appender;

        public Appender getAppender() {
            return this.appender;
        }

        public void setAppender(Appender value) {
            this.appender = value;
        }
    }
}

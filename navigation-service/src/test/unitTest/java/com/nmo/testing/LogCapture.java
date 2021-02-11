package com.sixthday.testing;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.OutputStreamAppender;
import org.apache.logging.log4j.core.layout.*;

import java.io.ByteArrayOutputStream;
import java.util.Objects;
import java.util.UUID;

import static org.apache.logging.log4j.LogManager.getLogger;

public class LogCapture {

    public static ByteArrayOutputStream captureLogOutput(Class<?> classToLog, String logPattern) {
        Logger logger = (Logger) getLogger(classToLog);
        logger.setLevel(Level.ALL);

        LogCaptureOutputStream loggingOutput = new LogCaptureOutputStream();
        OutputStreamAppender appender = getBasicAppender(loggingOutput, logPattern);
        loggingOutput.setAppender(appender);
        logger.addAppender(appender);

        return loggingOutput;
    }

    public static ByteArrayOutputStream captureLogOutput(Class<?> classToLog) {
        return captureLogOutput(classToLog, null);
    }

    private static OutputStreamAppender getBasicAppender(ByteArrayOutputStream loggingOutput, String logPattern) {
        PatternLayout patternLayout = getBasicPatternLayout();
        if (Objects.nonNull(logPattern)) {
            patternLayout = getPatternLayout(logPattern);
        }
        OutputStreamAppender appender = OutputStreamAppender.newBuilder().setName(UUID.randomUUID().toString()).setTarget(loggingOutput).setLayout(patternLayout).build();
        appender.start();
        return appender;
    }

    private static PatternLayout getBasicPatternLayout() {
        PatternMatch[] patterns = {PatternMatch.newBuilder().setKey("ContentSyncLogMarker")
                .setPattern(
                        "%d{dd MMM yyyy HH:mm:ss,SSS} %-5p [%c] (%t) %notEmpty{MessageId=\"%X{CS.messageId}\", MessageType=\"%X{CS.messageType}\", ContextId=\"%X{CS.contextId}\", OriginTimestamp=\"%X{CS.originTimestamp}\", NMOSRC=\"%X{CS.NMOSRC}\", NMODEST=\"%X{CS.NMODEST}\", NMORESOURCE=\"%X{CS.NMORESOURCE}\",} %msg%n%throwable").build(),
                PatternMatch.newBuilder().setKey("NoMDCLogMarker")
                        .setPattern("%d{dd MMM yyyy HH:mm:ss,SSS} %-5p [%c] (%t) %msg%n%throwable").build()};
        PatternLayout patternLayout = PatternLayout.newBuilder()
                .withPatternSelector(MarkerPatternSelector.newBuilder().setDefaultPattern("[%level] %msg%n").setProperties(patterns).build()).build();
        return patternLayout;
    }

    private static PatternLayout getPatternLayout(String pattern) {
        PatternLayout patternLayout = PatternLayout.newBuilder().withPattern(pattern).build();
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

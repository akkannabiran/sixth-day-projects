package com.sixthday;

import org.jboss.logging.MDC;
import org.slf4j.*;

import java.time.Clock;
import java.util.Map;
import java.util.function.Supplier;

public class sixthdayLogging {

    public static final String MDC_MESSAGE_ID = "CS.messageId";
    public static final String MDC_MESSAGE_TYPE = "CS.messageType";
    public static final String MDC_CONTEXT_ID = "CS.contextId";
    public static final String MDC_ORIGIN_TIMESTAMP = "CS.originTimestamp";
    public static final String MDC_SOURCE_PROPERTY = "CS.sixthdaySRC";
    public static final String MDC_DESTINATION_PROPERTY = "CS.sixthdayDEST";
    public static final String MDC_RESOURCE_PROPERTY = "CS.sixthdayRESOURCE";
    public static final String CONTENT_SYNC_ES_RESOURCE = "ElasticSearch";
    public static final Marker CONTENT_SYNC_LOG_MAKER = MarkerFactory.getMarker("ContentSyncLogMarker");
    private static final String EVENT_TYPE = "event_type";
    private static final String OPERATION = "operation";
    private static final String STATE = "state";
    private static final String STATUS = "Status";
    private static final String DURATION = "duration";
    private static final String SUCCESS = "Success";
    private static final String BEGIN = "BEGIN";
    private static final String END = "END";
    private static final String FAILED = "Failed";

    private sixthdayLogging() {
    }

    private static void resetMdc(Map<String, Object> originalMDC) {
        MDC.clear();
        for (Map.Entry<String, Object> entry : originalMDC.entrySet()) {
            MDC.put(entry.getKey(), entry.getValue());
        }
    }

    public static void logError(Logger log, EventType eventType, OperationType operationType) {
        logError(log, eventType, operationType, "");
    }

    public static void logError(Logger log, EventType eventType, OperationType operationType, String message) {
        logError(log, Clock.systemUTC(), eventType, operationType, message, null);
    }

    public static void logError(Logger log, EventType eventType, OperationType operationType, String message, Exception e) {
        logError(log, Clock.systemUTC(), eventType, operationType, message, e);
    }

    private static void logError(Logger log, Clock clock, EventType eventType, OperationType operationType, String message, Exception e) {
        Map<String, Object> originalMDC = MDC.getMap();
        long start = clock.millis();
        if (eventType != null) {
            MDC.put(EVENT_TYPE, eventType);
        }
        if (operationType != null) {
            MDC.put(OPERATION, operationType);
        }
        MDC.put(STATE, BEGIN);
        log.error(message);
        MDC.put(STATUS, FAILED);
        long duration = clock.millis() - start;
        MDC.put(DURATION, duration);
        MDC.put(STATE, END);
        log.error((e != null && e.getCause() != null) ? e.getCause().getMessage() : message, e);
        resetMdc(originalMDC);
    }

    public static <T> T logOperation(Logger log, EventType eventType, OperationType operationType, Supplier<T> operation) {
        return logOperation(log, eventType, operationType, "", operation);
    }

    public static <T> T logOperation(Logger log, EventType eventType, OperationType operationType, String message, Supplier<T> operation) {
        return logOperation(log, Clock.systemUTC(), eventType, operationType, message, operation);
    }

    private static <T> T logOperation(Logger log, Clock clock, EventType eventType, OperationType operationType, String message, Supplier<T> operation) {
        Map<String, Object> originalMDC = MDC.getMap();
        long start = clock.millis();
        boolean isException = false;
        try {
            if (eventType != null) {
                MDC.put(EVENT_TYPE, eventType);
            }
            if (operationType != null) {
                MDC.put(OPERATION, operationType);
            }
            MDC.put(STATE, BEGIN);
            T result = operation.get();
            MDC.put(STATUS, SUCCESS);
            return result;
        } catch (Exception e) {
            MDC.put(STATUS, FAILED);
            isException = true;
            throw e;
        } finally {
            long duration = clock.millis() - start;
            MDC.put(DURATION, duration);
            MDC.put(STATE, END);
            if (isException) {
                log.error(message);
            } else {
                log.info(message);
            }
            resetMdc(originalMDC);
        }
    }

    public static <T> T logDebugOperation(Logger log, EventType eventType, OperationType operationType, Supplier<T> operation) {
        return logDebugOperation(log, eventType, operationType, "", operation);
    }

    public static <T> T logDebugOperation(Logger log, EventType eventType, OperationType operationType, String message, Supplier<T> operation) {
        return logDebugOperation(log, Clock.systemUTC(), eventType, operationType, message, operation);
    }

    private static <T> T logDebugOperation(Logger log, Clock clock, EventType eventType, OperationType operationType, String message, Supplier<T> operation) {
        Map<String, Object> originalMDC = MDC.getMap();
        long start = clock.millis();
        boolean exceptioned = false;
        try {
            if (eventType != null) {
                MDC.put(EVENT_TYPE, eventType);
            }
            if (operationType != null) {
                MDC.put(OPERATION, operationType);
            }
            MDC.put(STATE, BEGIN);

            log.debug(message);
            T result = operation.get();
            MDC.put(STATUS, SUCCESS);
            return result;
        } catch (Exception e) {
            MDC.put(STATUS, FAILED);
            exceptioned = true;
            throw e;
        } finally {
            long duration = clock.millis() - start;
            MDC.put(DURATION, duration);
            MDC.put(STATE, END);
            if (exceptioned)
                log.error(message);
            else
                log.debug(message);
            resetMdc(originalMDC);
        }
    }

    public enum EventType {
        API,
        ATG_API,
        DTMESSAGE,
        BUILD_LEFTNAV_ON_STARTUP,
        ON_EVENT

    }

    public enum OperationType {
        ATG_FETCH_NAVIGATION_TREE,
        TRANSFORM_MOBILE_SILO,
        TRANSFORM_DESKTOP_SILO,
        SILO_SCHEDULER,
        FIND_PATHS,
        BUILD_LEFTNAV,
        BUILD_DI,
        SAVE_DI,
        GET_DI,
        SAVE_DI_DYNAMO,
        SKIP_SAVE_DI_DYNAMO,
        GET_DI_DYNAMO,
        GET_DI_DYNAMO_CACHE,
        SAVE_LEFTNAV,
        S3_UPDATE_SILO,
        CATEGORY_DOCUMENT_MESSAGE_RECEIVED,
        ELASTICSEARCH_GET_CATEGORY_DOCUMENT,
        ELASTICSEARCH_GET_CATEGORY_DOCUMENTS,
        ELASTICSEARCH_GET_CATEGORY_DOCUMENTS_BY_SCROLL,
        ELASTICSEARCH_UPSERT_LEFTNAV_DOCUMENT,
        ELASTICSEARCH_GET_PATHS_BY_REFERENCEID,
        GET_LEFTNAV_PATHS,
        GET_LEFTNAV,
        BUILD_DESIGNER_INDEX,
        GET_DESIGNER_INDEX,
        GET_CATEGORY,
        GET_PENDING_NODES,
        PROCESS_PENDING_NODES
    }
}
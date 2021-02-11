package com.sixthday.store.repository;

import com.sixthday.logger.logging.Loggable;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.update.UpdateResponse;
import org.slf4j.MDC;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static com.sixthday.store.config.Constants.Logging.CONTENT_SYNC_LOG_MAKER;

@Loggable
@Slf4j
public class ESUpdateActionListener implements ActionListener<UpdateResponse> {
    private static final String SUCCESS_LOG_FORMAT = "sixthdayLogType=\"ContentSyncDashboard\", OperationType=\"ES_UPDATE\", Status=\"Success\", DurationInMs=\"{}\"";
    private static final String FAILURE_LOG_FORMAT = "sixthdayLogType=\"ContentSyncDashboard\", OperationType=\"ES_UPDATE\", Status=\"Failed\", DurationInMs=\"{}\", Error=\"{}\"";
    private Instant start;
    private Map<String, String> copyOfContextMap;
    public ESUpdateActionListener(Instant start, Map<String, String> copyOfContextMap) {
        this.start = start;
        this.copyOfContextMap = copyOfContextMap;
    }

    @Override
    public void onResponse(UpdateResponse response) {
        MDC.setContextMap(copyOfContextMap);
        log.debug("Execute call to elasticsearch was successful with response={}", response.toString());
        log.info(CONTENT_SYNC_LOG_MAKER, SUCCESS_LOG_FORMAT, Duration.between(start, Instant.now()).toMillis());
    }

    @Override
    public void onFailure(Exception e) {
        MDC.setContextMap(copyOfContextMap);
        log.error(CONTENT_SYNC_LOG_MAKER, FAILURE_LOG_FORMAT, Duration.between(start, Instant.now()).toMillis(), e.getMessage(), e);
    }
}

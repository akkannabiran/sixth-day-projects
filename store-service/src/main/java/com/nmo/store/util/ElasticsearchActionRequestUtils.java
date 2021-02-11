package com.sixthday.store.util;

import static com.sixthday.store.config.Constants.Logging.CONTENT_SYNC_LOG_MAKER;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.slf4j.MDC;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ElasticsearchActionRequestUtils {
  public static final String SUCCESS_LOG_FORMAT = "sixthdayLogType=\"ContentSyncDashboard\", OperationType=\"ES_UPDATE\", Status=\"Success\", DurationInMs=\"{}\"";
  public static final String FAILURE_LOG_FORMAT = "sixthdayLogType=\"ContentSyncDashboard\", OperationType=\"ES_UPDATE\", Status=\"Failed\",  DurationInMs=\"{}\", Error=\"{}\"";
  
  private ElasticsearchActionRequestUtils() {}
  
  @SuppressWarnings({"unchecked" , "rawtypes"})
  public static void executeAsync(ActionRequestBuilder actionRequestBuilder) {
    final Map<String, String> copyOfContextMap = MDC.getCopyOfContextMap();
    actionRequestBuilder.execute(new ActionListener<ActionResponse>() {
      private Instant start = Instant.now();
      
      @Override
      public void onResponse(ActionResponse response) {
        MDC.setContextMap(copyOfContextMap);
        if (response instanceof BulkResponse) {
          if (((BulkResponse) response).hasFailures()) {
            log.error(CONTENT_SYNC_LOG_MAKER, FAILURE_LOG_FORMAT, Duration.between(start, Instant.now()).toMillis(), ((BulkResponse) response).buildFailureMessage());
          } else {
            log.info(CONTENT_SYNC_LOG_MAKER, SUCCESS_LOG_FORMAT, Duration.between(start, Instant.now()).toMillis());
          }
        } else if (response instanceof UpdateResponse) {
          log.debug("Execute call to elasticsearch was successful with response={}", response.toString());
          log.info(CONTENT_SYNC_LOG_MAKER, SUCCESS_LOG_FORMAT, Duration.between(start, Instant.now()).toMillis());
        }
      }
      
      @Override
      public void onFailure(Exception e) {
        MDC.setContextMap(copyOfContextMap);
        log.error(CONTENT_SYNC_LOG_MAKER, FAILURE_LOG_FORMAT, Duration.between(start, Instant.now()).toMillis(), e.getMessage(), e);
      }
    });
  }
}

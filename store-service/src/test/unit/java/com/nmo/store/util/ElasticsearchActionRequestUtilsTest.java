package com.sixthday.store.util;

import static com.sixthday.store.SplunkFieldsAssertionUtil.verifySplunkFieldsOnLog;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.MDC;

import com.sixthday.store.LogCapture;

import lombok.SneakyThrows;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({"unchecked" , "rawtypes"})
public class ElasticsearchActionRequestUtilsTest {
  
  private ByteArrayOutputStream loggingOutput;
  @Mock
  private ActionRequestBuilder actionRequestBuilder;
  @Captor
  private ArgumentCaptor<ActionListener> actionListenerArgumentCaptor;
  
  @BeforeClass
  public static void setLoggerContextSelector() {
    System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.selector.BasicContextSelector");
  }
  
  @Before
  public void setup() {
    MDC.clear();
    loggingOutput = LogCapture.captureLogOutput(ElasticsearchActionRequestUtils.class);
    System.setOut(new PrintStream(loggingOutput));
  }
  
  @After
  public void tearDown() {
    LogCapture.stopLogCapture(ElasticsearchActionRequestUtils.class, loggingOutput);
  }
  
  @Test
  @SneakyThrows
  public void shouldExecuteAnActionRequestAsynchronouslyWithActionListenerAsCallback() {
    ElasticsearchActionRequestUtils.executeAsync(actionRequestBuilder);
    verify(actionRequestBuilder, only()).execute(actionListenerArgumentCaptor.capture());
  }
  
  @Test
  @SneakyThrows
  public void shouldLogSuccessWhenUpdateRequestCallbackIsSuccessful() {
    ElasticsearchActionRequestUtils.executeAsync(actionRequestBuilder);
    UpdateResponse updateResponseMock = Mockito.mock(UpdateResponse.class);
    String successMessage = "SUCCESS";
    
    when(updateResponseMock.toString()).thenReturn(successMessage);
    
    verify(actionRequestBuilder, only()).execute(actionListenerArgumentCaptor.capture());
    
    actionListenerArgumentCaptor.getValue().onResponse(updateResponseMock);
    String actualLog = new String(loggingOutput.toByteArray());
    assertThat(actualLog, containsString(successMessage));
  }
  
  @Test
  @SneakyThrows
  public void shouldLogSuccessWhenBulkRequestCallbackIsSuccessful() {
    MDC.put("messageId", "MessageId");
    MDC.put("messageType", "Something");
    ElasticsearchActionRequestUtils.executeAsync(actionRequestBuilder);
    BulkResponse bulkResponseMock = Mockito.mock(BulkResponse.class);
    
    verify(actionRequestBuilder, only()).execute(actionListenerArgumentCaptor.capture());
    
    actionListenerArgumentCaptor.getValue().onResponse(bulkResponseMock);
    String actualLog = new String(loggingOutput.toByteArray());
    String[] expected = {"OperationType=\"ES_UPDATE\"" , "MessageId=\"MessageId\"," , "MessageType=\"Something\"," , "Status=\"Success\""};
    verifySplunkFieldsOnLog(actualLog, expected);
  }
  
  @Test
  @SneakyThrows
  public void shouldLogErrorWhenBulkRequestCallbackIsUnsuccessful() {
    String failed = "FAILED";
    ElasticsearchActionRequestUtils.executeAsync(actionRequestBuilder);
    BulkResponse bulkResponseMock = Mockito.mock(BulkResponse.class);
    
    when(bulkResponseMock.hasFailures()).thenReturn(true);
    when(bulkResponseMock.buildFailureMessage()).thenReturn(failed);
    
    verify(actionRequestBuilder, only()).execute(actionListenerArgumentCaptor.capture());
    
    actionListenerArgumentCaptor.getValue().onResponse(bulkResponseMock);
    String actualLog = new String(loggingOutput.toByteArray());
    String[] expected = {"OperationType=\"ES_UPDATE\"" , "Status=\"Failed\"," , "Error=\"FAILED\""};
    verifySplunkFieldsOnLog(actualLog, expected);
  }
  
  @Test
  @SneakyThrows
  public void shouldLogErrorWhenExecuteActionRequestCallbackIsUnsuccessful() {
    ElasticsearchActionRequestUtils.executeAsync(actionRequestBuilder);
    
    verify(actionRequestBuilder, times(1)).execute(actionListenerArgumentCaptor.capture());
    
    String failedMessage = "FAILED";
    actionListenerArgumentCaptor.getValue().onFailure(new Exception(failedMessage));
    String actualLog = new String(loggingOutput.toByteArray());
    assertThat(actualLog, containsString(failedMessage));
  }
  
  @Test
  @SneakyThrows
  public void shouldRetainMDCvaluesWhenExecuteActionRequestCallbackIsSuccessful() {
    MDC.put("messageId", "MessageId");
    MDC.put("messageType", "Something");
    ElasticsearchActionRequestUtils.executeAsync(actionRequestBuilder);
    verify(actionRequestBuilder, times(1)).execute(actionListenerArgumentCaptor.capture());
    MDC.clear();
    
    actionListenerArgumentCaptor.getValue().onResponse(Mockito.mock(UpdateResponse.class));
    String actualLog = new String(loggingOutput.toByteArray());
    String[] expected = {"OperationType=\"ES_UPDATE\"" , "MessageId=\"MessageId\"," , "MessageType=\"Something\"," ,};
    verifySplunkFieldsOnLog(actualLog, expected);
  }
  
  @Test
  @SneakyThrows
  public void shouldRetainMDCvaluesWhenExecuteActionRequestCallbackIsUnsuccessful() {
    MDC.put("messageId", "MessageId");
    MDC.put("messageType", "Something");
    ElasticsearchActionRequestUtils.executeAsync(actionRequestBuilder);
    verify(actionRequestBuilder, times(1)).execute(actionListenerArgumentCaptor.capture());
    MDC.clear();
    
    actionListenerArgumentCaptor.getValue().onFailure(new Exception());
    String actualLog = new String(loggingOutput.toByteArray());
    String[] expected = {"OperationType=\"ES_UPDATE\"" , "MessageId=\"MessageId\"," , "MessageType=\"Something\"," , "Status=\"Failed\""};
    verifySplunkFieldsOnLog(actualLog, expected);
  }
  
}

package com.sixthday.navigation.integration.receiver;

import com.sixthday.navigation.batch.designers.processor.DesignerIndexProcessor;
import com.sixthday.navigation.batch.processor.LeftNavTreeProcessor;
import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import com.sixthday.navigation.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.integration.messages.CategoryMessage;
import com.sixthday.navigation.integration.utils.MDCUtils;
import com.sixthday.testing.LogCapture;
import com.sixthday.testing.SplunkFieldsAssertionUtil;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.json.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.amqp.core.Message;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MDCUtils.class)
@PowerMockIgnore("javax.management.*")
public class CategoryDocumentReceiverTest {
    private ByteArrayOutputStream loggingOutput;
    @Mock
    private Message message;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Channel channel;

    @Mock
    private LeftNavTreeProcessor leftNavTreeProcessor;

    @Mock
    private DesignerIndexProcessor designerIndexProcessor;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NavigationBatchServiceConfig navigationBatchServiceConfig;

    @InjectMocks
    private CategoryDocumentReceiver categoryDocumentReceiver;

    @BeforeClass
    public static void setLoggerContextSelector() {
        System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.selector.BasicContextSelector");
    }

    @Before
    @SneakyThrows
    public void setUp() {
        when(navigationBatchServiceConfig.getRabbitmqConfig().getReceiver().getCategoryEvent().getQueueName()).thenReturn("NavigationBatchQueueName");
        when(navigationBatchServiceConfig.getLeftNavBatchConfig().isBuildOnEventReceiver()).thenReturn(true);
        when(channel.queueDeclare("NavigationBatchQueueName", true, false, false, Collections.emptyMap()).getMessageCount()).thenReturn(0);
        loggingOutput = LogCapture.captureLogOutput(CategoryDocumentReceiver.class);
        System.setOut(new PrintStream(loggingOutput));
    }

    @After
    public void tearDown() {
        LogCapture.stopLogCapture(CategoryDocumentReceiver.class, loggingOutput);
    }

    @Test
    @SneakyThrows
    public void shouldPopulateMDCValuesOnReceivingValidCategoryMessage() {
        PowerMockito.mockStatic(MDCUtils.class);
        String messageBody = new JSONObject()
                .put("id", "cat33090732d")
                .put("batchId", "SampleBatchId")
                .put("originTimestampInfo", new JSONObject(Collections.singletonMap("Category Message", "SampleOriginTimestampValue")))
                .put("eventType", "CATEGORY_REMOVED").toString();
        when(message.getBody()).thenReturn(messageBody.getBytes());
        categoryDocumentReceiver.onMessage(message, channel);
        verifyStatic(times(1));
        MDCUtils.setMDC("cat33090732d", "Category Message", "SampleBatchId", "SampleOriginTimestampValue", "RMQ:NavigationBatchQueueName", "ES:leftnav_index", "ElasticSearch");
    }

    @Test
    public void shouldPopulateMDCValuesOnReceivingInvalidCategoryMessage() {
        String messageBody = "WeDontExpectThisFromQueue";
        when(message.getBody()).thenReturn(messageBody.getBytes());
        when(message.getBody()).thenReturn(messageBody.getBytes());
        PowerMockito.mockStatic(MDCUtils.class);
        categoryDocumentReceiver.onMessage(message, channel);
        verifyStatic(times(1));
        MDCUtils.setMDCOnMessageParsingFailure("RMQ:NavigationBatchQueueName", "ES:leftnav_index", "ElasticSearch");
    }

    @Test
    @SneakyThrows
    public void shouldGiveContentSyncSuccessLogOnReceivingValidCategoryMessage() {
        String messageBody = new JSONObject()
                .put("id", "cat33090732d")
                .put("displayName", "FeaturedDesigners")
                .put("batchId", "SampleBatchId")
                .put("originTimestampInfo", new JSONObject(Collections.singletonMap("Category Message", "SampleOriginTimestampValue")))
                .put("eventType", "CATEGORY_REMOVED").toString();
        when(message.getBody()).thenReturn(messageBody.getBytes());
        categoryDocumentReceiver.onMessage(message, channel);
        String actualLog = new String(loggingOutput.toByteArray());
        String[] expected = {"CategoryDocumentReceiverUpdate=\"CATEGORY_DOCUMENT_MESSAGE_RECEIVED\",",
                "Status=\"Success\",",
                "MessageId=\"cat33090732d\",",
                "MessageType=\"Category Message\",",
                "ContextId=\"SampleBatchId\",",
                "OriginTimestamp=\"SampleOriginTimestampValue\",",
                "DurationInMs=\"",
                "sixthdaySRC=\"RMQ:NavigationBatchQueueName\"", "sixthdayDEST=\"ES:leftnav_index\"", "sixthdayRESOURCE=\"ElasticSearch\""};
        SplunkFieldsAssertionUtil.verifySplunkFieldsOnLog(actualLog, expected);
    }

    @Test
    @SneakyThrows
    public void shouldGiveContentSyncFailureLogWhenErrorProcessingCategoryMessage() {
        String messageBody = new JSONObject()
                .put("id", "cat33090732d")
                .put("displayName", "FeaturedDesigners")
                .put("batchId", "SampleBatchId")
                .put("originTimestampInfo", new JSONObject(Collections.singletonMap("Category Message", "SampleOriginTimestampValue")))
                .put("eventType", "CATEGORY_REMOVED").toString();
        when(message.getBody()).thenReturn(messageBody.getBytes());

        doThrow(new RuntimeException("ExpectedFromTestCase")).when(leftNavTreeProcessor).startByEvent(any(CategoryMessage.class), anyBoolean());

        categoryDocumentReceiver.onMessage(message, channel);
        String actualLog = new String(loggingOutput.toByteArray());
        String[] expected = {"CategoryDocumentReceiverUpdate=\"CATEGORY_DOCUMENT_MESSAGE_RECEIVED\",",
                "Status=\"Failed\",",
                "MessageId=\"cat33090732d\",",
                "MessageType=\"Category Message\",",
                "ContextId=\"SampleBatchId\",",
                "OriginTimestamp=\"SampleOriginTimestampValue\",",
                "DurationInMs=\"",
                "sixthdaySRC=\"RMQ:NavigationBatchQueueName\"", "sixthdayDEST=\"ES:leftnav_index\"", "sixthdayRESOURCE=\"ElasticSearch\"", "Error=\"ExpectedFromTestCase\""};
        SplunkFieldsAssertionUtil.verifySplunkFieldsOnLog(actualLog, expected);
    }

    @Test
    @SneakyThrows
    public void shouldGiveContentSyncFailureLogOnReceivingInvalidCategoryMessage() {
        String messageBody = "WeDontExpectThisFromQueue";
        when(message.getBody()).thenReturn(messageBody.getBytes());
        categoryDocumentReceiver.onMessage(message, channel);
        String actualLog = new String(loggingOutput.toByteArray());
        String[] expected = {"CategoryDocumentReceiverUpdate=\"CATEGORY_DOCUMENT_MESSAGE_RECEIVED\",",
                "Status=\"Failed\",",
                "MessageId=\"-\",",
                "MessageType=\"DeserializationFailed\",",
                "ContextId=\"-\",",
                "OriginTimestamp=\"NA\",",
                "DurationInMs=\"",
                "sixthdaySRC=\"RMQ:NavigationBatchQueueName\"", "sixthdayDEST=\"ES:leftnav_index\"", "sixthdayRESOURCE=\"ElasticSearch\""};
        SplunkFieldsAssertionUtil.verifySplunkFieldsOnLog(actualLog, expected);
    }

    @Test
    public void verifyStartByEventIsCalled() {
        String messageBody = new JSONObject().toString();
        when(message.getBody()).thenReturn(messageBody.getBytes());
        when(navigationBatchServiceConfig.getLeftNavBatchConfig().isBuildOnEventReceiver()).thenReturn(true);

        categoryDocumentReceiver.onMessage(message, channel);

        verify(leftNavTreeProcessor, times(1)).startByEvent(any(CategoryDocument.class), anyBoolean());
    }

    @Test
    public void verifyStartByEventIsNotCalled() {
        String messageBody = new JSONObject().toString();
        when(message.getBody()).thenReturn(messageBody.getBytes());
        when(navigationBatchServiceConfig.getLeftNavBatchConfig().isBuildOnEventReceiver()).thenReturn(false);

        categoryDocumentReceiver.onMessage(message, channel);

        verify(leftNavTreeProcessor, times(0)).startByEvent(any(CategoryDocument.class), anyBoolean());
    }

    @Test
    public void testBuildDesignerIndexIsNotCalled() {
        String messageBody = new JSONObject().toString();
        when(message.getBody()).thenReturn(messageBody.getBytes());
        when(designerIndexProcessor.isRebuildDesignerIndex(any(CategoryDocument.class))).thenReturn(false);

        categoryDocumentReceiver.onMessage(message, channel);

        verify(designerIndexProcessor, times(0)).buildDesignerIndex();
    }

    @Test
    public void testBuildDesignerIndexIsCalled() {
        String messageBody = new JSONObject().toString();
        when(message.getBody()).thenReturn(messageBody.getBytes());
        when(designerIndexProcessor.isRebuildDesignerIndex(any(CategoryDocument.class))).thenReturn(true);

        categoryDocumentReceiver.onMessage(message, channel);

        verify(designerIndexProcessor, times(1)).buildDesignerIndex();
    }
}

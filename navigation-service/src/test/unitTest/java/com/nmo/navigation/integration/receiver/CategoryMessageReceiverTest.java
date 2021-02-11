package com.sixthday.navigation.integration.receiver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.models.SearchCriteria;
import com.sixthday.navigation.api.models.SearchCriteriaBuilder;
import com.sixthday.navigation.integration.config.PublisherConfiguration;
import com.sixthday.navigation.integration.exceptions.CategoryMessageProcessingException;
import com.sixthday.navigation.integration.messages.CategoryMessage;
import com.sixthday.navigation.integration.services.*;
import com.sixthday.navigation.integration.utils.MDCUtils;
import com.sixthday.testing.LogCapture;
import com.sixthday.testing.SplunkFieldsAssertionUtil;
import lombok.SneakyThrows;
import org.json.JSONArray;
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MDCUtils.class)
@PowerMockIgnore("javax.management.*")
public class CategoryMessageReceiverTest {

    @Mock
    private CategorySyncService categorySyncService;

    @Mock
    private LeftNavSyncService leftNavSyncService;

    @Mock
    private CategoryPublisherService categoryPublisherService;

    @Mock
    private Message message;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PublisherConfiguration publisherConfiguration;

    @InjectMocks
    private CategoryMessageReceiver categoryMessageReceiver;

    private SearchCriteria searchCriteria;
    private ByteArrayOutputStream loggingOutput;

    @BeforeClass
    public static void setLoggerContextSelector() {
        System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.selector.BasicContextSelector");
    }

    @Before
    public void setUp() {
        searchCriteria = new SearchCriteriaBuilder().build();
        loggingOutput = LogCapture.captureLogOutput(CategoryMessageReceiver.class);
        System.setOut(new PrintStream(loggingOutput));
    }

    @After
    public void tearDown() {
        LogCapture.stopLogCapture(CategoryMessageReceiver.class, loggingOutput);
    }

    @Test
    public void shouldListenToQueueAndUpdateElasticSearch() throws Exception {
        String messageBody = new JSONObject()
                .put("id", "cat33090732d")
                .put("eventType", "CATEGORY_UPDATED")
                .put("displayName", "Rommel Shoes")
                .put("parents", new JSONObject())
                .put("url", "/Shoes/Rommel-Shoes/cat33090732d/c.cat")
                .put("flags", new JSONArray())
                .put("searchCriteria", new JSONObject(new ObjectMapper().writeValueAsString(searchCriteria)))
                .put("batchId", "SampleBatchId")
                .put("originTimestampInfo", new JSONObject(Collections.singletonMap("Category Message", "SampleOriginTimestampValue")))
                .put("messageType", "Category Message").toString();

        when(message.getBody()).thenReturn(messageBody.getBytes());

        categoryMessageReceiver.onMessage(message, null);

        verify(categorySyncService).upsertOrDeleteCategory(any(CategoryDocument.class), eq(CategoryMessage.EventType.CATEGORY_UPDATED));
    }

    @Test
    public void shouldListenToQueueAndDeleteLeftNavWhenIsDeletedFlagIsTrue() throws Exception {
        String categoryToBeDeleted = "cat123";
        String messageBody = new JSONObject()
                .put("id", categoryToBeDeleted)
                .put("eventType", "CATEGORY_UPDATED")
                .put("displayName", "Rommel Shoes")
                .put("isDeleted", true)
                .put("parents", new JSONObject())
                .put("url", "/Shoes/Rommel-Shoes/cat33090732d/c.cat")
                .put("flags", new JSONArray())
                .put("searchCriteria", new JSONObject(new ObjectMapper().writeValueAsString(searchCriteria)))
                .put("batchId", "SampleBatchId")
                .put("originTimestampInfo", new JSONObject(Collections.singletonMap("Category Message", "SampleOriginTimestampValue")))
                .put("messageType", "Category Message").toString();

        when(message.getBody()).thenReturn(messageBody.getBytes());

        categoryMessageReceiver.onMessage(message, null);

        verify(leftNavSyncService).deleteLeftNavTreeForThatCategory(categoryToBeDeleted);
    }

    @Test
    public void shouldListenToQueueAndNotDeleteLeftNavWhenIsDeletedFlagIsFalse() throws Exception {
        String categoryToBeDeleted = "cat123";
        String messageBody = new JSONObject()
                .put("id", categoryToBeDeleted)
                .put("eventType", "CATEGORY_UPDATED")
                .put("displayName", "Rommel Shoes")
                .put("isDeleted", false)
                .put("parents", new JSONObject())
                .put("url", "/Shoes/Rommel-Shoes/cat33090732d/c.cat")
                .put("flags", new JSONArray())
                .put("searchCriteria", new JSONObject(new ObjectMapper().writeValueAsString(searchCriteria)))
                .put("batchId", "SampleBatchId")
                .put("originTimestampInfo", new JSONObject(Collections.singletonMap("Category Message", "SampleOriginTimestampValue")))
                .put("messageType", "Category Message").toString();

        when(message.getBody()).thenReturn(messageBody.getBytes());

        categoryMessageReceiver.onMessage(message, null);

        verify(leftNavSyncService, never()).deleteLeftNavTreeForThatCategory(any());
    }

    @Test
    public void shouldListenToQueueAndFailToUpdateElasticSearch() {
        String messageBody = "WeDontExpectThisFromQueue";
        when(message.getBody()).thenReturn(messageBody.getBytes());

        categoryMessageReceiver.onMessage(message, null);

        verify(categorySyncService, never()).upsertOrDeleteCategory(any(CategoryDocument.class), any());
        verify(leftNavSyncService, never()).deleteLeftNavTreeForThatCategory(any());
    }

    @Test
    public void shouldFailValidationWhenThereIsNoEventType() throws Exception {
        String messageBody = new JSONObject()
                .put("id", "cat33090732d")
                .put("displayName", "Rommel Shoes").toString();

        when(message.getBody()).thenReturn(messageBody.getBytes());

        categoryMessageReceiver.onMessage(message, null);

        verify(categorySyncService, never()).upsertOrDeleteCategory(any(CategoryDocument.class), any());
        verify(leftNavSyncService, never()).deleteLeftNavTreeForThatCategory(any());
    }

    @Test
    @SneakyThrows
    public void shouldFailValidationWhenThereIsInvalidEventType() {
        String messageBody = new JSONObject()
                .put("id", "cat33090732d")
                .put("displayName", "FeaturedDesigners")
                .put("searchCriteria", new JSONObject(new ObjectMapper().writeValueAsString(searchCriteria)))
                .put("batchId", "SampleBatchId")
                .put("originTimestampInfo", new JSONObject(Collections.singletonMap("Category Message", "SampleOriginTimestampValue")))
                .put("eventType", "CATEGORY_UPDATED").toString();

        when(message.getBody()).thenReturn(messageBody.getBytes());

        categoryMessageReceiver.onMessage(message, null);

        verify(categorySyncService, times(1)).upsertOrDeleteCategory(any(CategoryDocument.class), any());
        verify(leftNavSyncService, never()).deleteLeftNavTreeForThatCategory(any());
    }

    @Test
    @SneakyThrows
    public void shouldNotThrowExceptionWhenMessageHasNoContent() {
        when(message.getBody()).thenReturn(null);

        categoryMessageReceiver.onMessage(message, null);

        verify(categorySyncService, never()).upsertOrDeleteCategory(any(CategoryDocument.class), any());
        verify(leftNavSyncService, never()).deleteLeftNavTreeForThatCategory(any());
    }

    @Test
    public void shouldIncrementMessageCountWhenValidMessageIsReceivedWithEventTypeCategoryUpdated() throws Exception {
        String messageBody = new JSONObject()
                .put("id", "cat33090732d")
                .put("displayName", "FeaturedDesigners")
                .put("searchCriteria", new JSONObject(new ObjectMapper().writeValueAsString(searchCriteria)))
                .put("batchId", "SampleBatchId")
                .put("originTimestampInfo", new JSONObject(Collections.singletonMap("Category Message", "SampleOriginTimestampValue")))
                .put("eventType", "CATEGORY_UPDATED").toString();

        when(message.getBody()).thenReturn(messageBody.getBytes());

        categoryMessageReceiver.onMessage(message, null);

        verify(categorySyncService, times(1)).upsertOrDeleteCategory(any(CategoryDocument.class), any());
        assertEquals(1, categoryMessageReceiver.getCategoryMessagesCount());
    }

    @Test
    public void shouldIncrementMessageCountWhenValidMessageIsReceivedWithEventTypeCategoryRemoved() throws Exception {
        String messageBody = new JSONObject()
                .put("id", "cat33090732d")
                .put("displayName", "FeaturedDesigners")
                .put("searchCriteria", new JSONObject(new ObjectMapper().writeValueAsString(searchCriteria)))
                .put("batchId", "SampleBatchId")
                .put("originTimestampInfo", new JSONObject(Collections.singletonMap("Category Message", "SampleOriginTimestampValue")))
                .put("eventType", "CATEGORY_REMOVED").toString();

        when(message.getBody()).thenReturn(messageBody.getBytes());

        categoryMessageReceiver.onMessage(message, null);

        verify(categorySyncService, times(1)).upsertOrDeleteCategory(any(CategoryDocument.class), any());
        assertEquals(1, categoryMessageReceiver.getCategoryMessagesCount());
    }

    @Test
    public void shouldReadSearchCriteriaFromCategoryMessage() throws Exception {
        String messageBody = new JSONObject()
                .put("id", "cat33090732d")
                .put("displayName", "FeaturedDesigners")
                .put("searchCriteria", new JSONObject(new ObjectMapper().writeValueAsString(searchCriteria)))
                .put("batchId", "SampleBatchId")
                .put("originTimestampInfo", new JSONObject(Collections.singletonMap("Category Message", "SampleOriginTimestampValue")))
                .put("eventType", "CATEGORY_UPDATED").toString();

        when(message.getBody()).thenReturn(messageBody.getBytes());

        categoryMessageReceiver.onMessage(message, null);

        ArgumentCaptor<CategoryDocument> captor = ArgumentCaptor.forClass(CategoryDocument.class);
        verify(categorySyncService, times(1)).upsertOrDeleteCategory(captor.capture(), any());
        CategoryDocument actual = captor.getValue();

        assertThat(actual.getSearchCriteria().getInclude().getAttributes().size(), is(searchCriteria.getInclude().getAttributes().size()));
        assertThat(actual.getSearchCriteria().getInclude().getHierarchy().size(), is(searchCriteria.getInclude().getHierarchy().size()));
        assertThat(actual.getSearchCriteria().getInclude().getPromotions().size(), is(searchCriteria.getInclude().getPromotions().size()));
        assertThat(actual.getSearchCriteria().getExclude().getAttributes().size(), is(searchCriteria.getExclude().getAttributes().size()));
        assertThat(actual.getSearchCriteria().getExclude().getHierarchy().size(), is(searchCriteria.getExclude().getHierarchy().size()));
        assertThat(actual.getSearchCriteria().getExclude().getPromotions().size(), is(searchCriteria.getExclude().getPromotions().size()));
    }

    @Test
    @SneakyThrows
    public void shouldPopulateMDCValuesOnReceivingValidCategoryMessage() {
        PowerMockito.mockStatic(MDCUtils.class);
        String messageBody = new JSONObject()
                .put("id", "cat33090732d")
                .put("displayName", "FeaturedDesigners")
                .put("searchCriteria", new JSONObject(new ObjectMapper().writeValueAsString(searchCriteria)))
                .put("batchId", "SampleBatchId")
                .put("originTimestampInfo", new JSONObject(Collections.singletonMap("Category Message", "SampleOriginTimestampValue")))
                .put("eventType", "CATEGORY_REMOVED").toString();
        when(message.getBody()).thenReturn(messageBody.getBytes());
        categoryMessageReceiver.onMessage(message, null);
        verifyStatic(times(1));
        MDCUtils.setMDC("cat33090732d", "Category Message", "SampleBatchId", "SampleOriginTimestampValue", "RMQ:sixthday-category", "ES:category_index", "ElasticSearch");
    }

    @Test
    public void shouldPopulateMDCValuesOnReceivingInvalidCategoryMessage() {
        PowerMockito.mockStatic(MDCUtils.class);
        String messageBody = "WeDontExpectThisFromQueue";
        when(message.getBody()).thenReturn(messageBody.getBytes());
        when(message.getBody()).thenReturn(messageBody.getBytes());

        try {
            categoryMessageReceiver.onMessage(message, null);
        } catch (Exception e) {
        }

        verifyStatic(times(1));
        MDCUtils.setMDCOnMessageParsingFailure("RMQ:sixthday-category", "ES:category_index", "ElasticSearch");
    }

    @Test
    @SneakyThrows
    public void shouldGiveContentSyncSuccessLogOnReceivingValidCategoryMessage() {
        String messageBody = new JSONObject()
                .put("id", "cat33090732d")
                .put("displayName", "FeaturedDesigners")
                .put("searchCriteria", new JSONObject(new ObjectMapper().writeValueAsString(searchCriteria)))
                .put("batchId", "SampleBatchId")
                .put("originTimestampInfo", new JSONObject(Collections.singletonMap("Category Message", "SampleOriginTimestampValue")))
                .put("eventType", "CATEGORY_REMOVED").toString();
        when(message.getBody()).thenReturn(messageBody.getBytes());
        categoryMessageReceiver.onMessage(message, null);
        String actualLog = new String(loggingOutput.toByteArray());
        String[] expected = {"CategoryMessageReceiverUpdate=\"CATEGORY_MESSAGE_RECEIVED\",",
                "Status=\"Success\",",
                "MessageId=\"cat33090732d\",",
                "MessageType=\"Category Message\",",
                "ContextId=\"SampleBatchId\",",
                "OriginTimestamp=\"SampleOriginTimestampValue\",",
                "DurationInMs=\"",
                "NMOSRC=\"RMQ:sixthday-category\"", "NMODEST=\"ES:category_index\"", "NMORESOURCE=\"ElasticSearch\""};
        SplunkFieldsAssertionUtil.verifySplunkFieldsOnLog(actualLog, expected);
    }

    @Test
    @SneakyThrows
    public void shouldGiveContentSyncFailureLogWhenErrorProcessingCategoryMessage() {
        String messageBody = new JSONObject()
                .put("id", "cat33090732d")
                .put("displayName", "FeaturedDesigners")
                .put("searchCriteria", new JSONObject(new ObjectMapper().writeValueAsString(searchCriteria)))
                .put("batchId", "SampleBatchId")
                .put("originTimestampInfo", new JSONObject(Collections.singletonMap("Category Message", "SampleOriginTimestampValue")))
                .put("eventType", "CATEGORY_REMOVED").toString();
        when(message.getBody()).thenReturn(messageBody.getBytes());

        doThrow(new CategoryMessageProcessingException(null, "ExpectedFromTestCase")).when(categorySyncService).upsertOrDeleteCategory(any(), any());

        categoryMessageReceiver.onMessage(message, null);
        String actualLog = new String(loggingOutput.toByteArray());
        String[] expected = {"CategoryMessageReceiverUpdate=\"CATEGORY_MESSAGE_RECEIVED\",",
                "Status=\"Failed\",",
                "MessageId=\"cat33090732d\",",
                "MessageType=\"Category Message\",",
                "ContextId=\"SampleBatchId\",",
                "OriginTimestamp=\"SampleOriginTimestampValue\",",
                "DurationInMs=\"",
                "NMOSRC=\"RMQ:sixthday-category\"", "NMODEST=\"ES:category_index\"", "NMORESOURCE=\"ElasticSearch\"", "Error=\"ExpectedFromTestCase\""};
        SplunkFieldsAssertionUtil.verifySplunkFieldsOnLog(actualLog, expected);
    }

    @Test
    @SneakyThrows
    public void shouldGiveContentSyncFailureLogOnReceivingInvalidCategoryMessage() {
        String messageBody = "WeDontExpectThisFromQueue";
        when(message.getBody()).thenReturn(messageBody.getBytes());
        categoryMessageReceiver.onMessage(message, null);
        String actualLog = new String(loggingOutput.toByteArray());
        String[] expected = {"CategoryMessageReceiverUpdate=\"CATEGORY_MESSAGE_RECEIVED\",",
                "Status=\"Failed\",",
                "MessageId=\"-\",",
                "MessageType=\"DeserializationFailed\",",
                "ContextId=\"-\",",
                "OriginTimestamp=\"NA\",",
                "DurationInMs=\"",
                "NMOSRC=\"RMQ:sixthday-category\"", "NMODEST=\"ES:category_index\"", "NMORESOURCE=\"ElasticSearch\""};
        SplunkFieldsAssertionUtil.verifySplunkFieldsOnLog(actualLog, expected);
    }
}
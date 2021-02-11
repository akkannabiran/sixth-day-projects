package com.sixthday.navigation.integration.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.models.SearchCriteria;
import com.sixthday.navigation.api.elasticsearch.models.SearchCriteriaOptions;
import com.sixthday.testing.LogCapture;
import com.sixthday.testing.SplunkFieldsAssertionUtil;
import lombok.SneakyThrows;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.MDC;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;

import static com.sixthday.navigation.config.Constants.MDC_CONTEXT_ID;
import static com.sixthday.navigation.config.Constants.MDC_ORIGIN_TIMESTAMP;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MDC.class)
@PowerMockIgnore("javax.management.*")
public class CategoryMessagePublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private CategoryMessagePublisher categoryMessagePublisher;

    private ObjectMapper ob = new ObjectMapper();
    private ByteArrayOutputStream loggingOutput;

    @BeforeClass
    public static void setLoggerContextSelector() {
        System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.selector.BasicContextSelector");
    }

    @Before
    public void setUp() {
        loggingOutput = LogCapture.captureLogOutput(CategoryMessagePublisher.class);
        System.setOut(new PrintStream(loggingOutput));
    }

    @After
    public void tearDown() {
        LogCapture.stopLogCapture(CategoryMessagePublisher.class, loggingOutput);
    }

    @Test
    @SneakyThrows
    public void shouldSendAMessage() {
        PowerMockito.mockStatic(MDC.class);
        PowerMockito.when(MDC.get(MDC_CONTEXT_ID)).thenReturn("MOCK_CONTEXT_ID");
        PowerMockito.when(MDC.get(MDC_ORIGIN_TIMESTAMP)).thenReturn("MOCK_TIMESTAMP");
        CategoryDocument categoryDocument = new CategoryDocument();
        categoryDocument.setId("Cat1234");
        SearchCriteria sc = new SearchCriteria();
        sc.setInclude(new SearchCriteriaOptions(Collections.singletonList("PROMO1234"), null, null));
        categoryDocument.setSearchCriteria(sc);
        categoryMessagePublisher.sendMessage(categoryDocument);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(captor.capture());
        String messageSent = captor.getValue();
        assertThat("Published message should have ContextId", messageSent, containsString("MOCK_CONTEXT_ID"));
        CategoryDocument published = ob.readValue(messageSent.getBytes(), CategoryDocument.class);
        assertThat("Category properties should be present", published.getId(), equalTo(categoryDocument.getId()));
    }

    @Test
    @SneakyThrows
    public void shouldGiveContentSyncSuccessLogOnPublishingCategoryDocumentSuccessfully() {
        PowerMockito.mockStatic(MDC.class);
        PowerMockito.when(MDC.get(MDC_CONTEXT_ID)).thenReturn("MOCK_CONTEXT_ID");
        PowerMockito.when(MDC.get(MDC_ORIGIN_TIMESTAMP)).thenReturn("MOCK_TIMESTAMP");
        CategoryDocument categoryDocument = new CategoryDocument();
        categoryDocument.setId("Test_Cat_Id");

        categoryMessagePublisher.sendMessage(categoryDocument);

        String actualLog = new String(loggingOutput.toByteArray());
        String[] expected = {"CategoryMessagePublisherUpdate=\"CATEGORY_MESSAGE_PUBLISHED\",",
                "Status=\"Success\"",
                "MessageId=\"Test_Cat_Id\",",
                "MessageType=\"Category Message\",",
                "ContextId=\"MOCK_CONTEXT_ID\",",
                "OriginTimestamp=\"MOCK_TIMESTAMP\",",
                "DurationInMs=\"",
                "NMOSRC=\"APP:navigation-service\"", "NMODEST=\"RMQ:navigation-service.category\"", "NMORESOURCE=\"-\""};
        SplunkFieldsAssertionUtil.verifySplunkFieldsOnLog(actualLog, expected);
    }

    @Test
    @SneakyThrows
    public void shouldGiveContentSyncFailureLogWhenErrorPublishingCategoryDocument() {
        PowerMockito.mockStatic(MDC.class);
        PowerMockito.when(MDC.get(MDC_CONTEXT_ID)).thenReturn("MOCK_CONTEXT_ID");
        PowerMockito.when(MDC.get(MDC_ORIGIN_TIMESTAMP)).thenReturn("MOCK_TIMESTAMP");
        CategoryDocument categoryDocument = new CategoryDocument();
        categoryDocument.setId("Test_Cat_Id");
        doThrow(new AmqpException("SimulatedErrorFromRabbitMQ")).when(rabbitTemplate).convertAndSend(any());

        try {
            categoryMessagePublisher.sendMessage(categoryDocument);
        } catch (Exception e) {
        }

        String actualLog = new String(loggingOutput.toByteArray());
        String[] expected = {"CategoryMessagePublisherUpdate=\"CATEGORY_MESSAGE_PUBLISHED\",",
                "Status=\"Failed\"",
                "MessageId=\"Test_Cat_Id\",",
                "MessageType=\"Category Message\",",
                "ContextId=\"MOCK_CONTEXT_ID\",",
                "OriginTimestamp=\"MOCK_TIMESTAMP\",",
                "DurationInMs=\"",
                "NMOSRC=\"APP:navigation-service\"", "NMODEST=\"RMQ:navigation-service.category\"", "NMORESOURCE=\"-\"", "Error=\"SimulatedErrorFromRabbitMQ\""};
        SplunkFieldsAssertionUtil.verifySplunkFieldsOnLog(actualLog, expected);
    }
}
package com.sixthday.navigation.bootstrap;

import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.testing.LogCapture;
import lombok.SneakyThrows;
import org.elasticsearch.ResourceAlreadyExistsException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({RestHighLevelClient.class, ApplicationPreparedEventListener.class, NavigationServiceConfig.class, CreateIndexRequest.class, CreateIndexResponse.class, IndicesClient.class})
public class ApplicationPreparedEventListenerTest {

    private static final String indexName = "category_index";

    private ApplicationPreparedEventListener applicationPreparedEventListener;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private RestHighLevelClient client;

    private ByteArrayOutputStream loggingOutput;
    @Mock
    private ClassPathResource classPathResource;
    @Mock
    private ApplicationPreparedEvent preparedEvent;
    @Mock
    private CreateIndexRequest createIndexRequest;
    @Mock
    private CreateIndexResponse createIndexResponse;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private GetIndexRequest getIndexRequest;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private NavigationServiceConfig.ElasticSearchConfig elasticSearchConfig;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private NavigationServiceConfig navigationServiceConfig;

    @BeforeClass
    public static void setLoggerContextSelector() {
        System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.selector.BasicContextSelector");
    }

    @Before
    @SneakyThrows
    public void setUp() {
        when(navigationServiceConfig.getElasticSearchConfig()).thenReturn(elasticSearchConfig);
        when(elasticSearchConfig.getIndexName()).thenReturn(indexName);
        whenNew(CreateIndexRequest.class).withArguments(indexName).thenReturn(createIndexRequest);
        when(createIndexRequest.source(anyString(), any(XContentType.class))).thenReturn(createIndexRequest);
        when(createIndexResponse.isAcknowledged()).thenReturn(true);
        applicationPreparedEventListener = new ApplicationPreparedEventListener(client, navigationServiceConfig);

        loggingOutput = LogCapture.captureLogOutput(ApplicationPreparedEventListener.class);
    }

    @After
    public void tearDown() {
        System.out.println(new String(loggingOutput.toByteArray()));
        LogCapture.stopLogCapture(ApplicationPreparedEventListener.class, loggingOutput);
    }

    @Test
    public void shouldCreateTheIndexAndPushTheMappingsToElasticSearch() {

        try {
            assertThat(new CreateIndexRequest(indexName));
            verifyNew(CreateIndexRequest.class).withArguments(indexName);
            when(client.indices().create(createIndexRequest, RequestOptions.DEFAULT)).thenReturn(createIndexResponse);
        } catch (Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
        applicationPreparedEventListener.onApplicationEvent(preparedEvent);

    }

    @Test
    @SneakyThrows
    public void shouldLogTheInfoIfESIndexAlreadyExists() {
        when(client.indices().exists(eq(getIndexRequest), any(RequestOptions.class))).thenReturn(Boolean.FALSE);
        when(client.indices().create(eq(createIndexRequest), any(RequestOptions.class)))
                .thenThrow(new ResourceAlreadyExistsException("Index exists"));
        applicationPreparedEventListener.onApplicationEvent(preparedEvent);
        String actualLog = new String(loggingOutput.toByteArray());
        assertThat(actualLog).contains("ElasticSearch Index \"" + indexName + "\" already exists");
    }

    @Test(expected = Exception.class)
    @SneakyThrows
    public void shouldReThrowErrorWhenNotAbleToUpdateMapping() {
        when(client.indices().create(createIndexRequest, RequestOptions.DEFAULT)).thenThrow(Exception.class);
        applicationPreparedEventListener.onApplicationEvent(preparedEvent);
    }

    @SuppressWarnings("squid:S00108")
    @Test(expected = RuntimeException.class)
    @SneakyThrows
    public void shouldLogErrorWhenNotAbleToUpdateMapping() {
        when(client.indices().putMapping(new PutMappingRequest(indexName), RequestOptions.DEFAULT))
                .thenThrow(new RuntimeException("Failed to update the mapping for ElasticSearch"));
        applicationPreparedEventListener.onApplicationEvent(preparedEvent);
        String actualLog = new String(loggingOutput.toByteArray());
    assertThat(actualLog).contains("Failed to update the mapping for ElasticSearch");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldLogWhenItsUnableToReadMappingsFile() throws Exception {
        whenNew(ClassPathResource.class).withAnyArguments().thenReturn(classPathResource);
        when(classPathResource.exists()).thenReturn(false);
        applicationPreparedEventListener.onApplicationEvent(preparedEvent);

        String actualLog = new String(loggingOutput.toByteArray());
        assertThat(actualLog).contains("Can not read resource file: " + classPathResource.getFilename());

    }

}
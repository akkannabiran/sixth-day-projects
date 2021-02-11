package com.sixthday.navigation.bootstrap;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.sixthday.model.serializable.designerindex.DesignerIndex;
import com.sixthday.navigation.config.AwsDynamoDbConfig;
import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import com.sixthday.testing.LogCapture;
import lombok.SneakyThrows;
import org.elasticsearch.ResourceAlreadyExistsException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.client.*;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;


@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({RestHighLevelClient.class, ApplicationPreparedEventListener.class, NavigationBatchServiceConfig.class, IndicesClient.class,
    CreateIndexResponse.class, TableUtils.class})
public class ApplicationPreparedEventListenerTest {

    private static final String leftNavIndexName = "leftnav_index";
    private ApplicationPreparedEventListener applicationPreparedEventListener;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private RestHighLevelClient client;
    private ByteArrayOutputStream loggingOutput;
    @Mock
    private ClassPathResource classPathResource;
    @Mock
    private CreateIndexRequest createIndexRequest;
    @Mock
    private CreateIndexResponse createIndexResponse;
    @Mock
    private GetIndexRequest getIndexRequest;
    @Mock
    private ApplicationPreparedEvent preparedEvent;

    @Mock
    private AwsDynamoDbConfig awsDynamoDbConfig;
    @Mock
    private AmazonDynamoDB amazonDynamoDB;
    @Mock
    private DynamoDBMapper dynamoDbMapper;

    @Mock
    CreateTableRequest createTableRequest;

    @BeforeClass
    public static void setLoggerContextSelector() {
        System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.selector.BasicContextSelector");
    }

    @Before
    public void setUp() throws Exception {
        NavigationBatchServiceConfig navigationBatchServiceConfig = mock(NavigationBatchServiceConfig.class, new ReturnsDeepStubs());
        when(navigationBatchServiceConfig.getElasticSearchConfig().getLeftNavIndex().getName()).thenReturn(leftNavIndexName);

        whenNew(GetIndexRequest.class).withAnyArguments().thenReturn(getIndexRequest);
        when(getIndexRequest.indices(anyString())).thenReturn(getIndexRequest);
        when(client.indices().exists(eq(getIndexRequest), any(RequestOptions.class))).thenReturn(Boolean.FALSE);

        whenNew(CreateIndexRequest.class).withArguments(leftNavIndexName).thenReturn(createIndexRequest);
        when(createIndexRequest.source(anyString(), any(XContentType.class))).thenReturn(createIndexRequest);

        when(client.indices().create(any(CreateIndexRequest.class), any(RequestOptions.class))).thenReturn(createIndexResponse);
        when(createIndexResponse.isAcknowledged()).thenReturn(true);

        applicationPreparedEventListener = new ApplicationPreparedEventListener(client, navigationBatchServiceConfig, awsDynamoDbConfig,
            amazonDynamoDB, dynamoDbMapper);

        loggingOutput = LogCapture.captureLogOutput(ApplicationPreparedEventListener.class);

        when(dynamoDbMapper.generateCreateTableRequest(DesignerIndex.class)).thenReturn(createTableRequest);
        when(createTableRequest.withProvisionedThroughput(any(ProvisionedThroughput.class))).thenReturn(createTableRequest);
        when(createTableRequest.getTableName()).thenReturn("anyTable");
        when(amazonDynamoDB.createTable(any(CreateTableRequest.class))).thenReturn(null);
        mockStatic(TableUtils.class);
        when(TableUtils.createTableIfNotExists(any(AmazonDynamoDB.class), any(CreateTableRequest.class))).thenReturn(true);
    }

    @After
    public void tearDown() {
        System.out.println(new String(loggingOutput.toByteArray()));
        LogCapture.stopLogCapture(ApplicationPreparedEventListener.class, loggingOutput);
    }

    @Test
    public void shouldCreateTheIndexAndPushTheMappingsToElasticSearch() throws Exception {
        applicationPreparedEventListener.onApplicationEvent(preparedEvent);

        verifyNew(CreateIndexRequest.class).withArguments(leftNavIndexName);

        verify(client.indices()).create(createIndexRequest, RequestOptions.DEFAULT);
    }

    @Test
    public void shouldLogTheInfoIfESIndexAlreadyExists() throws Exception {
        when(client.indices().exists(eq(getIndexRequest), any(RequestOptions.class))).thenReturn(Boolean.FALSE);

        when(client.indices().create(eq(createIndexRequest), any(RequestOptions.class)))
                .thenThrow(new ResourceAlreadyExistsException("Index exists"));
        applicationPreparedEventListener.onApplicationEvent(preparedEvent);

        String actualLog = new String(loggingOutput.toByteArray());
        assertThat(actualLog).contains("ElasticSearch Index \"" + leftNavIndexName + "\" already exists");
    }

    @Test(expected = Exception.class)
    @SneakyThrows
    public void shouldReThrowErrorWhenNotAbleToUpdateMapping() {
        when(client.indices().create(createIndexRequest, RequestOptions.DEFAULT)).thenThrow(Exception.class);
        applicationPreparedEventListener.onApplicationEvent(preparedEvent);
    }

    @SuppressWarnings("squid:S00108")
    @Test
    @SneakyThrows
    public void shouldLogErrorWhenNotAbleToUpdateMapping() {
        when(client.indices().create(createIndexRequest, RequestOptions.DEFAULT)).thenThrow(Exception.class);
        try {
            applicationPreparedEventListener.onApplicationEvent(preparedEvent);
        } catch (Exception ex) {
        }

        String actualLog = new String(loggingOutput.toByteArray());
        assertThat(actualLog).containsSequence("Failed to create ElasticSearch index", "from file");
    }

    @Test
    public void shouldLogWhenItsUnableToReadMappingsFile() throws Exception {
        whenNew(ClassPathResource.class).withAnyArguments().thenReturn(classPathResource);
        when(classPathResource.exists()).thenReturn(false);

        applicationPreparedEventListener.onApplicationEvent(preparedEvent);

        String actualLog = new String(loggingOutput.toByteArray());
        assertThat(actualLog).contains("Unable to read file with mappings and settings for \"" + leftNavIndexName + "\" index");
    }
}
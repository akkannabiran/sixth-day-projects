package com.sixthday.navigation.integration.repository;

import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.integration.config.PublisherConfiguration;
import com.sixthday.navigation.integration.config.SubscriberConfiguration;
import com.sixthday.navigation.integration.messages.CategoryMessage;
import com.sixthday.navigation.integration.services.CategoryPublisherService;
import lombok.SneakyThrows;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({RestHighLevelClient.class})
public class CategorySyncRepositoryTest {
    @InjectMocks
    private CategorySyncRepository categorySyncRepository;
    @Mock
    private RestHighLevelClient restHighLevelClient;
    @Mock
    private NavigationServiceConfig.ElasticSearchConfig elasticSearchConfig;
    @Mock
    private SubscriberConfiguration subscriberConfiguration;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PublisherConfiguration publisherConfiguration;
    @Mock
    private CategoryPublisherService categoryPublisherService;
    @Mock
    private BulkResponse bulkResponse;

    @Before
    public void setup() {
        when(subscriberConfiguration.elasticSearchClient()).thenReturn(restHighLevelClient);
        when(subscriberConfiguration.getElasticSearchConfig()).thenReturn(elasticSearchConfig);
        when(subscriberConfiguration.getElasticSearchConfig().getIndexName()).thenReturn("index");
    }

    @Test
    @SneakyThrows
    public void shouldCallBulkUploadForCategoryUpdate() {
        categorySyncRepository = new CategorySyncRepository(subscriberConfiguration, publisherConfiguration, categoryPublisherService);
        categorySyncRepository.upsertOrDeleteCategoryDocument(CategoryDocument.builder().id("cat1").build(), CategoryMessage.EventType.CATEGORY_UPDATED);
        verify(restHighLevelClient).bulk(any(BulkRequest.class), any(RequestOptions.class));
    }

    @Test
    @SneakyThrows
    public void shouldCallBulkUploadForCategoryDelete() {
        categorySyncRepository = new CategorySyncRepository(subscriberConfiguration, publisherConfiguration, categoryPublisherService);
        categorySyncRepository.upsertOrDeleteCategoryDocument(CategoryDocument.builder().id("cat1").build(), CategoryMessage.EventType.CATEGORY_REMOVED);
        verify(restHighLevelClient).bulk(any(BulkRequest.class), any(RequestOptions.class));
    }

    @Test
    @SneakyThrows
    public void shouldNotPublishTheMessageWhenBulkResponseHasFailure() {
        when(restHighLevelClient.bulk(any(BulkRequest.class), any(RequestOptions.class))).thenReturn(bulkResponse);
        when(bulkResponse.hasFailures()).thenReturn(true);
        categorySyncRepository = new CategorySyncRepository(subscriberConfiguration, publisherConfiguration, categoryPublisherService);
        categorySyncRepository.upsertOrDeleteCategoryDocument(CategoryDocument.builder().id("cat1").build(), CategoryMessage.EventType.CATEGORY_REMOVED);
        verify(categoryPublisherService, never()).buildAndSend(any(CategoryDocument.class));
    }

    @Test
    @SneakyThrows
    public void shouldPublishTheMessageWhenBulkResponseHasFailureAndQueueEnabled() {
        when(restHighLevelClient.bulk(any(BulkRequest.class), any(RequestOptions.class))).thenReturn(bulkResponse);
        when(bulkResponse.hasFailures()).thenReturn(false);
        when(publisherConfiguration.getRabbitmqConfig().getPublisher().getCategoryEvent().isEnabled()).thenReturn(true);
        categorySyncRepository = new CategorySyncRepository(subscriberConfiguration, publisherConfiguration, categoryPublisherService);
        categorySyncRepository.upsertOrDeleteCategoryDocument(CategoryDocument.builder().id("cat1").build(), CategoryMessage.EventType.CATEGORY_REMOVED);
        verify(categoryPublisherService).buildAndSend(any(CategoryDocument.class));
    }

    @Test
    @SneakyThrows
    public void shouldNotPublishTheMessageWhenBulkResponseHasFailureAndQueueDisabled() {
        when(restHighLevelClient.bulk(any(BulkRequest.class), any(RequestOptions.class))).thenReturn(bulkResponse);
        when(bulkResponse.hasFailures()).thenReturn(false);
        when(publisherConfiguration.getRabbitmqConfig().getPublisher().getCategoryEvent().isEnabled()).thenReturn(false);
        categorySyncRepository = new CategorySyncRepository(subscriberConfiguration, publisherConfiguration, categoryPublisherService);
        categorySyncRepository.upsertOrDeleteCategoryDocument(CategoryDocument.builder().id("cat1").build(), CategoryMessage.EventType.CATEGORY_REMOVED);
        verify(categoryPublisherService, never()).buildAndSend(any(CategoryDocument.class));
    }

}
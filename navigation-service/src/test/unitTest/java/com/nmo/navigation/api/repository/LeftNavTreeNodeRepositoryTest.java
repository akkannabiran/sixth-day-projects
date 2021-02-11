package com.sixthday.navigation.api.repository;

import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.api.elasticsearch.repository.CategoryRepository;
import com.sixthday.navigation.api.elasticsearch.repository.ESLeftNavRepository;
import com.sixthday.navigation.api.exceptions.LeftNavTreeNotFoundException;
import com.sixthday.navigation.elasticsearch.documents.LeftNavDocument;
import lombok.SneakyThrows;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


import static com.sixthday.navigation.api.data.LeftNavTreeTestDataFactory.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({RestHighLevelClient.class, RestClient.class, ESLeftNavRepository.class, NavigationServiceConfig.class})
public class LeftNavTreeNodeRepositoryTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NavigationServiceConfig navigationServiceConfig;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestHighLevelClient restHighLevelClient;

    @Mock
    private GetRequestBuilder getRequestBuilder;

    @Mock
    private GetResponse getResponse;

    @Mock
    private GetResponse getRequest;

    private ESLeftNavRepository leftNavRepository;
    
    private CategoryRepository categoryRepository;

    @Mock
    LeftNavDocument leftNavDocument;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private NavigationServiceConfig.ElasticSearchConfig elasticSearchConfig;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private NavigationServiceConfig.ElasticSearchConfig.IndexConfig indexConfig;

    @Before
    @SneakyThrows
    public void setUp() {
        when(navigationServiceConfig.getElasticSearchConfig()).thenReturn(elasticSearchConfig);
        when(navigationServiceConfig.getElasticSearchConfig().getLeftNavIndex()).thenReturn(indexConfig);
        leftNavDocument = new LeftNavDocument();
        leftNavDocument.setId("cat1");
        leftNavDocument.setName("name1");
        when(indexConfig.getName()).thenReturn(LEFTNAV_INDEX);
        when(indexConfig.getDocumentType6()).thenReturn(DOCUMENT_TYPE);
        when(getRequestBuilder.get()).thenReturn(getResponse);
        when(getResponse.isExists()).thenReturn(true);
        when(getResponse.getSourceAsString()).thenReturn(LEFTNAV_DOCUMENT_AS_STRING);
        leftNavRepository = new ESLeftNavRepository(restHighLevelClient, navigationServiceConfig, categoryRepository);
    }

    @Test
    public void shouldGetLeftNavDocumentsWhenAvailable() {
        assertEquals(leftNavDocument.getId(), NAV_PATH);
        assertEquals(leftNavDocument.getName(), "name1");
    }

    @Test(expected = LeftNavTreeNotFoundException.class)
    @SneakyThrows
    public void shouldThrowExceptionWhenNavPathNotAvailable() {
        when(getRequestBuilder.get()).thenReturn(getResponse);
        when(getResponse.getSourceAsString()).thenReturn("NavPathNotAvailable");
        when(leftNavRepository.getLeftNavDocument(NAV_PATH)).thenThrow(new LeftNavTreeNotFoundException(NAV_PATH));
    }

    @Test
    @SneakyThrows
    public void shouldCallCloseClientMethodAtLeastOnce() {
        leftNavRepository.closeClient();
        verify(restHighLevelClient, atLeastOnce()).close();
    }

    @Test(expected = LeftNavTreeNotFoundException.class)
    public void shouldThrowLeftNavTreeNotFoundExceptionWhenParsingResponse() {
        when(getRequestBuilder.get()).thenReturn(getResponse);
        when(getResponse.getSourceAsString()).thenReturn("Error Parsing the document");
        when(leftNavRepository.getLeftNavDocument(NAV_PATH)).thenThrow(new LeftNavTreeNotFoundException(NAV_PATH));
    }
}

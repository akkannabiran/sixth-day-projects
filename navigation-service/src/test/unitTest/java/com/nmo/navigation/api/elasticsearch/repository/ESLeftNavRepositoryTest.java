package com.sixthday.navigation.api.elasticsearch.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.exceptions.CategoryNotFoundException;
import com.sixthday.navigation.api.exceptions.LeftNavTreeNotFoundException;
import com.sixthday.navigation.elasticsearch.documents.LeftNavDocument;
import lombok.SneakyThrows;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static com.sixthday.navigation.api.data.LeftNavTreeTestDataFactory.NAV_PATH;
import static com.sixthday.navigation.config.Constants.SOURCE_LEFT_NAV;
import static com.sixthday.navigation.config.Constants.US_COUNTRY_CODE;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;


@RunWith(PowerMockRunner.class)
@PrepareForTest({RestHighLevelClient.class, NavigationServiceConfig.class})
@PowerMockIgnore("javax.management.*")
public class ESLeftNavRepositoryTest {

    @InjectMocks
    private ESLeftNavRepository esLeftNavRepository;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NavigationServiceConfig navigationServiceConfig;
    @Mock
    private RestHighLevelClient restHighLevelClient;
    @Mock
    private GetResponse getResponse;
    @Mock
    private GetResponse getResponseForDefaultPath;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ESCategoryDocumentRepository esCategoryDocumentRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @Test
    @SneakyThrows
    public void shouldCloseRestClient() {
        esLeftNavRepository.closeClient();
        verify(restHighLevelClient, atLeastOnce()).close();
    }

    @Test
    @SneakyThrows
    public void shouldReturnLeftNavDocument() {
        when(restHighLevelClient.get(any(GetRequest.class), any(RequestOptions.class))).thenReturn(getResponse);
        when(getResponse.isExists()).thenReturn(true);
        when(getResponse.getSourceAsString()).thenReturn("{ \"key\":\"true\"}");
        when(objectMapper.readValue(any(String.class), eq(LeftNavDocument.class))).thenReturn(new LeftNavDocument());
        when(navigationServiceConfig.getElasticSearchConfig().getLeftNavIndex().getDocumentType6()).thenReturn("_doc");

        LeftNavDocument leftNavDocument = esLeftNavRepository.getLeftNavDocument("some path");
        assertNotNull(leftNavDocument);
    }

    @Test
    @SneakyThrows
    public void shouldReturnLeftNavDocumentForValidNavPath() {
    	when(restHighLevelClient.get(any(GetRequest.class), any(RequestOptions.class))).thenReturn(getResponse);
        when(getResponse.isExists()).thenReturn(true);
        when(getResponse.getSourceAsString()).thenReturn("{ \"id\":\"someid\"}");
        when(navigationServiceConfig.getElasticSearchConfig().getLeftNavIndex().getDocumentType6()).thenReturn("_doc");

        LeftNavDocument leftNavDocument = esLeftNavRepository.getLeftNavDocument("cat0_cat1_cat2_cat3");

        assertNotNull(leftNavDocument);
        assertThat(leftNavDocument.getId(), is("someid"));
        Mockito.verify(restHighLevelClient, times(1)).get(any(GetRequest.class), any(RequestOptions.class));
    }
    
    @Test
    @SneakyThrows
    public void shouldReturnDefaultLeftNavDocumentWhenWrongNavPathWithInvalidCategoryIdInNavPath() {
    	when(restHighLevelClient.get(any(GetRequest.class), any(RequestOptions.class))).thenReturn(getResponse, getResponseForDefaultPath);
        when(getResponse.isExists()).thenReturn(false);
        when(getResponseForDefaultPath.isExists()).thenReturn(true);
        CategoryDocument categoryDocument = mock(CategoryDocument.class);
        when(categoryDocument.getDefaultPath()).thenReturn("cat0_cat1_cat3");
        when(categoryRepository.getCategoryDocument("cat3")).thenReturn(categoryDocument);
        when(getResponseForDefaultPath.getSourceAsString()).thenReturn("{ \"id\":\"someid\"}");
        when(navigationServiceConfig.getElasticSearchConfig().getLeftNavIndex().getDocumentType6()).thenReturn("_doc");

        LeftNavDocument leftNavDocument = esLeftNavRepository.getLeftNavDocument("cat0_zzzzz_cat2_cat3");

        assertNotNull(leftNavDocument);
        assertThat(leftNavDocument.getId(), is("someid"));
        Mockito.verify(restHighLevelClient, times(2)).get(any(GetRequest.class), any(RequestOptions.class));
    }

    @Test(expected = LeftNavTreeNotFoundException.class)
    @SneakyThrows
    public void shouldThrowLeftNavTreeNotFoundExceptionWhenWrongNavPathAndNoLeftNavAvailableForDefaultPathLeftNav() {
        when(restHighLevelClient.get(any(GetRequest.class), any(RequestOptions.class))).thenReturn(getResponse, getResponseForDefaultPath);
        when(getResponse.isExists()).thenReturn(false);
        when(getResponseForDefaultPath.isExists()).thenReturn(false);
        CategoryDocument categoryDocument = mock(CategoryDocument.class);
        when(categoryDocument.getDefaultPath()).thenReturn("cat0_cat1_cat3");
        when(categoryRepository.getCategoryDocument("cat3")).thenReturn(categoryDocument);
        when(navigationServiceConfig.getElasticSearchConfig().getLeftNavIndex().getDocumentType6()).thenReturn("_doc");

        esLeftNavRepository.getLeftNavDocument("cat0_zzzzz_cat2_cat3");
    }

    @Test(expected = LeftNavTreeNotFoundException.class)
    @SneakyThrows
    public void shouldThrowLeftNavTreeNotFoundExceptionIfCategoryNotFoundWhenTryingToGetDefaultCategoryDoc() {
        when(restHighLevelClient.get(any(GetRequest.class), any(RequestOptions.class))).thenReturn(getResponse);
        when(getResponse.isExists()).thenReturn(false);
        CategoryDocument categoryDocument = mock(CategoryDocument.class);
        when(categoryDocument.getDefaultPath()).thenReturn("cat0_cat1_cat3");
        when(categoryRepository.getCategoryDocument("cat3")).thenThrow(CategoryNotFoundException.class);
        when(getResponseForDefaultPath.getSourceAsString()).thenReturn("{ \"id\":\"someid\"}");
        when(navigationServiceConfig.getElasticSearchConfig().getLeftNavIndex().getDocumentType6()).thenReturn("_doc");

        esLeftNavRepository.getLeftNavDocument("cat0_zzzzz_cat2_cat3");
    }
    
    @Test(expected = LeftNavTreeNotFoundException.class)
    @SneakyThrows
    public void shouldThrowExceptionWhenResponseIsNotLeftNavDocument() {
        when(restHighLevelClient.get(any(GetRequest.class), any(RequestOptions.class))).thenReturn(getResponse);
        when(getResponse.isExists()).thenReturn(true);
        when(getResponse.getSourceAsString()).thenReturn("some string");
        when(objectMapper.readValue(any(String.class), eq(LeftNavDocument.class))).thenReturn(new LeftNavDocument());
        when(navigationServiceConfig.getElasticSearchConfig().getLeftNavIndex().getDocumentType6()).thenReturn("_doc");

        esLeftNavRepository.getLeftNavDocument("some path");
    }

    @Test(expected = LeftNavTreeNotFoundException.class)
    @SneakyThrows
    public void shouldThrowExceptionWhenLeftNavDocumentIsNotPresent() {
        when(restHighLevelClient.get(any(GetRequest.class), any(RequestOptions.class))).thenReturn(getResponse);
        esLeftNavRepository.getLeftNavDocument("some path");
    }

}
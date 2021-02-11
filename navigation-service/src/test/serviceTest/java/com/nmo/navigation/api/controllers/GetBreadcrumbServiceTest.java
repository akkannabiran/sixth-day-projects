package com.sixthday.navigation.api.controllers;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.sixthday.navigation.api.controllers.elasticsearch.documents.CategoryDocumentBuilder;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.repository.CategoryRepository;
import com.sixthday.navigation.exceptions.NavigationServiceException;
import lombok.SneakyThrows;
import org.elasticsearch.action.get.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;

import static com.jayway.restassured.RestAssured.when;
import static com.sixthday.navigation.api.GetCategoryTestDataFactory.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.cloud.consul.enabled=false", "spring.cloud.vault.enabled=false"})
@TestPropertySource(properties = {"navigation.integration.mobile.cron=* * * * * *", "navigation.integration.desktop.cron=* * * * * *",
    "navigation.category-config.id-config.live=cat000000", "navigation.category-config.id-config.marketing=cat8900735", "navigation.category-config.id-config.stage=cat400731",
    "navigation.elastic-search-config.index-name=category_index", "navigation.elastic-search-config.document-type=category", "navigation.category-config.alternate-defaults.cat2=cat20"})
public class GetBreadcrumbServiceTest {

    @Value("${local.server.port}")
    int port;

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    CategoryRepository categoryRepository;

    @Mock
    private MultiGetRequestBuilder multiGetRequestBuilder;

    @Mock
    private MultiGetRequest multiGetRequest;

    @Mock
    private GetResponse getResponse;

    @Mock
    private MultiGetItemResponse multiGetItemResponse;

    @Mock
    private MultiGetResponse multiGetResponse;

    @MockBean
    private RestHighLevelClient restHighLevelClient;

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private SimpleMessageListenerContainer listenerContainer;

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private AmqpAdmin amqpAdmin;

    @MockBean(name = "categoryQueue", answer = Answers.RETURNS_DEEP_STUBS)
    private Queue categoryQueue;

    @MockBean(name = "categoryEventPublisherQueue", answer = Answers.RETURNS_DEEP_STUBS)
    private Queue categoryEventPublisherQueue;

    @MockBean(name = "connectionFactory", answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionFactory connectionFactory;

    @MockBean(name = "categoryEventPublisherConnectionFactory", answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionFactory categoryEventPublisherConnectionFactory;

    @Before
    @SneakyThrows
    public void setUp() {
        RestAssured.port = port;
        // Setting up mock response for elastic search library methods
        when(restHighLevelClient.mget(multiGetRequest, RequestOptions.DEFAULT)).thenReturn(multiGetResponse);
        when(multiGetRequestBuilder.add(CATEGORY_INDEX, DOCUMENT_TYPE, ITERABLE_CATEGORY_IDS)).thenReturn(multiGetRequestBuilder);
        when(multiGetRequestBuilder.get()).thenReturn(new MultiGetResponse(getTestMultiGetItemResponse(multiGetItemResponse)));
        when(multiGetItemResponse.getResponse()).thenReturn(getResponse);
        when(getResponse.isExists()).thenReturn(true);
        when(getResponse.getSourceAsString()).thenReturn(CATEGORY_DOCUMENT_AS_STRING_FOR_BREADCRUMBS);
    }

    @Test
    public void shouldServe500WhenParameterCategoryIdsNotPresentedInRequestPath() {
        when().get("/navigation/breadcrumbs")
                .then()
                .statusCode(500)
                .body("message", containsString("Required String parameter 'categoryIds' is not present"));
    }

    @Test
    public void shouldServe200WithBreadcrumbWhenCategoryIdsAvailable() {
        CategoryDocument cat1 = new CategoryDocumentBuilder()
                .withId("cat1")
                .withName("Ralph Lauren")
                .withCanonicalUrl("/Ralph-Lauren/cat1/c.cat")
                .withBoutique(false)
                .withContextualProperties(Collections.emptyList())
                .build();
        CategoryDocument cat2 = new CategoryDocumentBuilder()
                .withId("cat2")
                .withName("Lalph Rauren")
                .withCanonicalUrl("/Ralph-Lauren/Lalph-Rauren/cat2_cat1/c.cat")
                .withBoutique(false)
                .withContextualProperties(Collections.emptyList())
                .build();
        when(categoryRepository.getCategoryDocuments(Arrays.asList("cat1", "cat2"))).thenReturn(Arrays.asList(cat1, cat2));
        when(categoryRepository.getCategoryDocument("cat1")).thenReturn(cat1);
        when(categoryRepository.getCategoryDocument("cat2")).thenReturn(cat2);

        when().get("/navigation/breadcrumbs?categoryIds=cat1,cat2")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("breadcrumbs.size", is(2))
                .body("breadcrumbs[0].id", is("cat1"))
                .body("breadcrumbs[1].id", is("cat2"))
                .body("breadcrumbs[0].name", is("Ralph Lauren"))
                .body("breadcrumbs[1].name", is("Lalph Rauren"))
                .body("breadcrumbs[0].url", is("/Ralph-Lauren/cat1/c.cat?navpath=cat1&source=leftNav"))
                .body("breadcrumbs[1].url", is("/Ralph-Lauren/Lalph-Rauren/cat2_cat1/c.cat?navpath=cat1_cat2&source=leftNav"));
    }

    
    @Test
    public void shouldServe200WithTestGroupBreadcrumbWhenCategoryIdsAvailableForTestGroupRequest() {
        CategoryDocument cat1 = new CategoryDocumentBuilder()
                .withId("cat1")
                .withName("Ralph Lauren")
                .withCanonicalUrl("/Ralph-Lauren/cat1/c.cat")
                .withBoutique(false)
                .withContextualProperties(Collections.emptyList())
                .build();
        CategoryDocument cat2 = new CategoryDocumentBuilder()
                .withId("cat20")
                .withName("SomeAltName")
                .withCanonicalUrl("/Ralph-Lauren/Lalph-Rauren/cat2_cat1/c.cat")
                .withBoutique(false)
                .withContextualProperties(Collections.emptyList())
                .build();
        when(categoryRepository.getCategoryDocuments(Arrays.asList("cat1", "cat20"))).thenReturn(Arrays.asList(cat1, cat2));
        when(categoryRepository.getCategoryDocument("cat1")).thenReturn(cat1);
        when(categoryRepository.getCategoryDocument("cat20")).thenReturn(cat2);

        when().get("/navigation/breadcrumbs?categoryIds=cat1,cat2&navKeyGroup=B")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("breadcrumbs.size", is(2))
                .body("breadcrumbs[0].id", is("cat1"))
                .body("breadcrumbs[1].id", is("cat20"))
                .body("breadcrumbs[0].name", is("Ralph Lauren"))
                .body("breadcrumbs[1].name", is("SomeAltName"))
                .body("breadcrumbs[0].url", is("/Ralph-Lauren/cat1/c.cat?navpath=cat1&source=leftNav"))
                .body("breadcrumbs[1].url", is("/Ralph-Lauren/Lalph-Rauren/cat2_cat1/c.cat?navpath=cat1_cat2&source=leftNav"));
    }
    
    @Test
    public void shouldServe500WhenInvalidJSONServedByElastic() {
        NavigationServiceException navigationServiceException = new NavigationServiceException(null);
        when(categoryRepository.getCategoryDocuments(Arrays.asList("cat1", "cat2"))).thenThrow(navigationServiceException);
        when().get("/navigation/breadcrumbs?categoryIds=cat1,cat2")
                .then()
                .statusCode(500)
                .body("message", equalTo(null));
    }
}

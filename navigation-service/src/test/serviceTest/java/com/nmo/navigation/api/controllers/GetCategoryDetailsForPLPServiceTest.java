package com.sixthday.navigation.api.controllers;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.api.elasticsearch.repository.CategoryRepository;
import com.sixthday.navigation.api.mappers.PLPCategoryDetailsMapper;
import com.sixthday.navigation.api.models.response.SortOption;
import com.sixthday.navigation.config.S3Config;
import lombok.SneakyThrows;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static com.jayway.restassured.RestAssured.when;
import static com.sixthday.navigation.api.GetCategoryTestDataFactory.CATEGORY_DOCUMENT_AS_STRING_FOR_CATEGORY_DETAILS_PLP;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.cloud.consul.enabled=false", "spring.cloud.vault.enabled=false"})
@TestPropertySource(properties = {"navigation.integration.mobile.cron=* * * * * *", "navigation.integration.desktop.cron=* * * * * *", "/navigation.category-config.header-asset-url=/category/{categoryId}/r_header_long.html"})
public class GetCategoryDetailsForPLPServiceTest {

    private static final String CATEGORY_DETAILS_URL = "/navigation/categories/{categoryId}";
    private static final String CATEGORY_DETAILS_URL_WITH_PARENT_ID = "/navigation/categories/{categoryId}?parentCategoryId={parentCategoryId}";

    @Value("${local.server.port}")
    int port;

    @MockBean
    RestTemplate restTemplate;

    @MockBean
    RabbitTemplate rabbitTemplate;

    @MockBean
    S3Config s3Config;

    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    PLPCategoryDetailsMapper plpCategoryDetailsMapper;
    @Mock
    private GetRequestBuilder getRequestBuilder;
    @Mock
    private GetResponse getResponse;
    @MockBean
    private RestHighLevelClient restHighLevelClient;

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private NavigationServiceConfig navigationServiceConfig;

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

        when(navigationServiceConfig.getCategoryConfig().getHeaderAssetUrl()).thenReturn("/navigation/category/{categoryId}/r_header_long.html");
        when(restHighLevelClient.get(new GetRequest(anyString(), anyString(), anyString()), RequestOptions.DEFAULT)).thenReturn(getResponse);
        when(getRequestBuilder.get()).thenReturn(getResponse);
        when(getResponse.isExists()).thenReturn(true);
        when(getResponse.getSourceAsString()).thenReturn(CATEGORY_DOCUMENT_AS_STRING_FOR_CATEGORY_DETAILS_PLP);
    }

    @Test
    public void shouldServe200WhenOnlyCategoryIdIsAvailable() {
        when().get(CATEGORY_DETAILS_URL, new Object[]{"cat1"})
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", is("cat1"))
                .body("searchCriteria.include.hierarchy[0].level1", is("Women's Apparel"))
                .body("searchCriteria.include.hierarchy[0].level2", is("Dresses"))
                .body("searchCriteria.include.hierarchy[1].level1", is("Women's Apparel"))
                .body("searchCriteria.include.hierarchy[1].level2", is("Skirts"))
                .body("searchCriteria.include.attributes[0].Trends[0]", is("DT"))
                .body("searchCriteria.priceRange.min", is(1.5f))
                .body("searchCriteria.priceRange.max", is(2))
                .body("searchCriteria.priceRange.option", is("PROMO_PRICE"))
                .body("newArrivalLimit", is(1))
                .body("title", is("CategoryName"))
                .body("searchCriteria.productClassType", is("REGULAR_PRICE_ONLY"))
                .body("preferredProductIds[0]", is("prod1234"))
                .body("categoryHeaderAsset.hideOnMobile", is(true))
                .body("categoryHeaderAsset.categoryAssetUrl", is("/navigation/category/cat1/r_header_long.html"))
                .body("seo.nameOverride", is("nameOverride"))
                .body("seo.canonicalUrl", is("canonicalUrl"))
                .body("seo.title", is("seoTitle"))
                .body("seo.metaInformation", is("metaInformation"))
                .body("seo.titleOverride", is("titleOverride"))
                .body("seo.content", is("seoContent"))
                .body("applicableFilters[0].displayText", is("lifestyle"))
                .body("applicableFilters[0].filterKey", is("LifeStyle"))
                .body("applicableFilters[0].excludeFields[0]", is("abc"))
                .body("applicableFilters[0].values[0]", is("val1"))
                .body("applicableFilters[0].values[1]", is("val2"))
                .body("applicableFilters[0].values[2]", is("abc"))
                .body("availableSortOptions[0]." + SortOption.Option.PRICE_HIGH_TO_LOW, is(SortOption.Option.PRICE_HIGH_TO_LOW.getName()))
                .body("availableSortOptions[0].isDefault", is(false));
    }

    @Test
    public void shouldServe200WhenCategoryIdAndValidParentIdAreAvailableWithDesktopAlternateName() {
        when().get(CATEGORY_DETAILS_URL_WITH_PARENT_ID, new Object[]{"cat1", "parentCat00001"})
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", is("cat1"))
                .body("title", is("DesktopAlternateName"));
    }

    @Test
    public void shouldServe200WhenCategoryIdAndValidParentIdAreAvailableWithOutDesktopAlternateName() {
        when().get(CATEGORY_DETAILS_URL_WITH_PARENT_ID, new Object[]{"cat1", "parentCat00002"})
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", is("cat1"))
                .body("title", is("CategoryName"));
    }

    @Test
    public void shouldServe200WhenCategoryIdAndValidParentIdAreAvailableWithoutRedirectTypeAndUrl() {
        when().get(CATEGORY_DETAILS_URL_WITH_PARENT_ID, new Object[]{"cat1", "parentCat00002"})
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("seo", not(hasKey("redirectDetails")));
    }

    @Test
    public void shouldServe200WhenCategoryIdAndInValidParentIdAreAvailable() {
        when().get(CATEGORY_DETAILS_URL_WITH_PARENT_ID, new Object[]{"cat1", "parentCat00003"})
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", is("cat1"))
                .body("title", is("CategoryName"));
    }

    @Test
    public void shouldServe404CategoryNotFoundExceptionWhenParameterCategoryIdIsNotAvailable() {
        when(getResponse.isExists()).thenReturn(false);
        when().get("/navigation/categories/cat9999")
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("message", containsString("Category information is not available for the requested category cat9999"));
    }

    @Test
    public void shouldServe404WhenCategoryIdIsNull() {
        when().get("/navigation/categories/")
                .then()
                .statusCode(404);
    }
}

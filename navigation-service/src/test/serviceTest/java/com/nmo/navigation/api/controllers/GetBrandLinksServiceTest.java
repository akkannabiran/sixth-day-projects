package com.sixthday.navigation.api.controllers;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.sixthday.navigation.api.elasticsearch.repository.CategoryRepository;
import com.sixthday.navigation.config.S3Config;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static com.jayway.restassured.RestAssured.when;
import static com.sixthday.navigation.api.GetCategoryTestDataFactory.brandLinks;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.cloud.consul.enabled=false", "spring.cloud.vault.enabled=false"})
@TestPropertySource(properties = {"navigation.vault-config.elastic-search-user-name=abc", "navigation.vault-config.elastic-search-password=abc", "navigation.elastic-search-config.index-name=category_index", "navigation.elastic-search-config.document-type=category", "navigation.seo-footer-category-id=ftr000000"})
public class GetBrandLinksServiceTest {

    @Value("${local.server.port}")
    int port;

    @MockBean
    CategoryRepository categoryRepository;

    @MockBean
    RestTemplate restTemplate;

    @MockBean
    RabbitTemplate rabbitTemplate;

    @MockBean
    S3Config s3Config;

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
    public void setUp() {
        RestAssured.port = port;
        when(categoryRepository.getBrandLinks()).thenReturn(brandLinks());
    }

    @Test
    public void shouldServe200WithBrandLinks() {
        when().get("/navigation/brandlinks")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("sisterSites.size", is(1))
                .body("sisterSites[0].name", is("sisterSite 1"))
                .body("sisterSites[0].url", is("/url"))
                .body("sisterSites[0].topCategories.size", is(1))
                .body("sisterSites[0].topCategories[0].name", is("top cat"))
                .body("sisterSites[0].topCategories[0].url", is("/top-cat-url"));
    }
}

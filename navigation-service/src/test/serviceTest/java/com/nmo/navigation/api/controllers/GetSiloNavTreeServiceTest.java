package com.sixthday.navigation.api.controllers;

import com.jayway.restassured.RestAssured;
import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.config.S3Config;
import com.sixthday.navigation.domain.SiloNavTree;
import com.sixthday.navigation.integration.utils.AmazonS3ClientUtil;
import lombok.SneakyThrows;
import org.json.JSONArray;
import org.json.JSONObject;
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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"navigation.integration.mobile.cron=* * * * * *", "navigation.integration.desktop.cron=* * * * * *"})
public class GetSiloNavTreeServiceTest {

    @Value("${local.server.port}")
    int port;
    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    NavigationServiceConfig navigationServiceConfig;
    @MockBean
    S3Config s3Config;
    @Autowired
    CacheManager cacheManager;
    @MockBean
    RabbitTemplate rabbitTemplate;
    private String unitedStatesDesktopNavigationTreeId = "US_desktop";
    private String unitedStatesMobileNavigationTreeId = "US_mobile";
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
    @Mock
    private AmazonS3ClientUtil siloNavTreeRepository;

    @Before
    public void setup() {
        RestAssured.port = port;
        Cache navigationTreesCacheV1 = cacheManager.getCache("siloNavTreesV1");
        navigationTreesCacheV1.clear();
        Cache navigationTreesCacheV2 = cacheManager.getCache("siloNavTreesV2");
        navigationTreesCacheV2.clear();
    }

    @Test
    public void shouldCacheNavigationTree() {

        SiloNavTree siloNavTree = createNavigationTree(unitedStatesDesktopNavigationTreeId, "original-display-name");
        when(siloNavTreeRepository.getObject(unitedStatesDesktopNavigationTreeId)).thenReturn(siloNavTree.toString());

        when()
                .get("/navigation/silos/US/desktop")
                .then().statusCode(200)
                .body("silos.name", hasItems("original-display-name"));

        siloNavTree = createNavigationTree(unitedStatesDesktopNavigationTreeId, "other-display-name");
        when(siloNavTreeRepository.getObject(unitedStatesDesktopNavigationTreeId)).thenReturn(siloNavTree.toString());

        when()
                .get("/navigation/silos/US/desktop")
                .then().statusCode(200)
                .body("silos.name", hasItems("original-display-name"));
    }

    @Test
    public void shouldRespond404_whenNavigationTreeDataIsNotAvailableForACountry() {
        when()
                .get("/navigation/silos/US/desktop")
                .then()
                .statusCode(404)
                .body("message", containsString("US_desktop"))
                .body("statusCode", is(404));
    }

    @Test
    public void shouldServeMobileSiloDataFromRepository_whenDeviceTypeParamIsSetToMobileForACountry() {

        SiloNavTree siloNavTree = createNavigationTree(unitedStatesMobileNavigationTreeId);
        when(siloNavTreeRepository.getObject(unitedStatesMobileNavigationTreeId)).thenReturn(siloNavTree.toString());

        when()
                .get("/navigation/silos/US/mobile")
                .then().statusCode(200)
                .body("silos.name", hasItems("some-display-name"))
                .body("silos.url", hasItems("/some/url"));
    }

    @Test
    public void shouldServeDesktopSiloDataFromRepository_whenDeviceTypeParamIsSetToDesktopForACountry() {

        SiloNavTree siloNavTree = createNavigationTree(unitedStatesDesktopNavigationTreeId);
        when(siloNavTreeRepository.getObject(unitedStatesDesktopNavigationTreeId)).thenReturn(siloNavTree.toString());

        when()
                .get("/navigation/silos/US/desktop")
                .then().statusCode(200)
                .body("silos.name", hasItems("some-display-name"))
                .body("silos.url", hasItems("/some/url"));
    }

    @Test
    public void shouldServeMobileSiloDataFromRepository_whenDeviceTypeParamIsSetToMobileForACountryOnTestGroup() {
      String navKey = "US_mobile_B";
      SiloNavTree siloNavTree = createNavigationTree(navKey);
      when(siloNavTreeRepository.getObject(navKey)).thenReturn(siloNavTree.toString());
      
      when().get("/navigation/silos/US/mobile?navKeyGroup=B").then()
        .statusCode(200)
        .body("silos.name", hasItems("some-display-name"))
        .body("silos.url", hasItems("/some/url"));
    }

    @Test
    public void shouldServeMobileSiloDataFromRepository_whenDeviceTypeParamIsSetToMobileForACountryOnControlGroup() {
      String navKey = "US_mobile";
      SiloNavTree siloNavTree = createNavigationTree(navKey);
      when(siloNavTreeRepository.getObject(navKey)).thenReturn(siloNavTree.toString());
      
      when().get("/navigation/silos/US/mobile?navKeyGroup=A").then()
        .statusCode(200)
        .body("silos.name", hasItems("some-display-name"))
        .body("silos.url", hasItems("/some/url"));
    }
    
    @Test
    public void shouldServeMobileSiloDataFromRepository_whenDeviceTypeParamIsSetToMobileForACountryWithNoGroup() {
      String navKey = "US_mobile";
      SiloNavTree siloNavTree = createNavigationTree(navKey);
      when(siloNavTreeRepository.getObject(navKey)).thenReturn(siloNavTree.toString());
      
      when().get("/navigation/silos/US/mobile?navKeyGroup=").then()
        .statusCode(200)
        .body("silos.name", hasItems("some-display-name"))
        .body("silos.url", hasItems("/some/url"));
    }
    
    @Test
    public void shouldServeDesktopSiloDataFromRepository_whenDeviceTypeParamIsSetToDesktopForACountryOnControlGroup() {
      String navKey = "US_desktop";
      SiloNavTree siloNavTree = createNavigationTree(navKey);
      when(siloNavTreeRepository.getObject(navKey)).thenReturn(siloNavTree.toString());
      when().get("/navigation/silos/US/desktop?navKeyGroup=A").then()
        .statusCode(200)
        .body("silos.name", hasItems("some-display-name"))
        .body("silos.url", hasItems("/some/url"));
    }
    
    @Test
    public void shouldServeDesktopSiloDataFromRepository_whenDeviceTypeParamIsSetToDesktopForACountryOnTestGroup() {
      String navKey = "US_desktop_B";
      SiloNavTree siloNavTree = createNavigationTree(navKey);
      when(siloNavTreeRepository.getObject(navKey)).thenReturn(siloNavTree.toString());
      when().get("/navigation/silos/US/desktop?navKeyGroup=B").then()
        .statusCode(200)
        .body("silos.name", hasItems("some-display-name"))
        .body("silos.url", hasItems("/some/url"));
    }
    
    @Test
    public void shouldServeDesktopSiloDataFromRepository_whenDeviceTypeParamIsSetToDesktopForACountryWithNoGroup() {
      String navKey = "US_desktop";
      SiloNavTree siloNavTree = createNavigationTree(navKey);
      when(siloNavTreeRepository.getObject(navKey)).thenReturn(siloNavTree.toString());
      when().get("/navigation/silos/US/desktop?navKeyGroup=").then()
        .statusCode(200)
        .body("silos.name", hasItems("some-display-name"))
        .body("silos.url", hasItems("/some/url"));
    }
    
    @Test
    @SneakyThrows
    public void shouldRespond500_whenBadNavigationTreeDataRetrievedFromRepoForACountry() {

        SiloNavTree siloNavTree = new SiloNavTree(unitedStatesDesktopNavigationTreeId, new JSONObject().put("notARealProperty", "blahblahblah").toString());
        when(siloNavTreeRepository.getObject(unitedStatesDesktopNavigationTreeId)).thenReturn(siloNavTree.toString());

        when()
                .get("/navigation/silos/US/desktop")
                .then()
                .statusCode(500)
                .body("message", containsString("Unrecognized field \"notARealProperty\""))
                .body("statusCode", is(500));
    }

    private SiloNavTree createNavigationTree(final String navigationTreeId) {
        return createNavigationTree(navigationTreeId, "some-display-name");
    }

    @SneakyThrows
    private SiloNavTree createNavigationTree(final String navigationTreeId, String siloName) {

        final String nav_data = new JSONObject()
                .put("silos", new JSONArray()
                        .put(new JSONObject()
                                .put("id", "some-category-id")
                                .put("level", "0")
                                .put("name", siloName)
                                .put("url", "/some/url"))).toString();

        return new SiloNavTree(navigationTreeId, nav_data);
    }

    @Test
    public void shouldRespond404_whenNavigationTreeDataIsNotAvailable() {
        when()
                .get("/navigation/silos?deviceType=mobile&countryCode=US")
                .then()
                .statusCode(404)
                .body("message", containsString("US_mobile"))
                .body("statusCode", is(404));
    }

    @Test
    public void shouldServeDesktopSiloDataFromRepository_whenDeviceTypeParamIsNotSet() {

        SiloNavTree siloNavTree = createNavigationTree(unitedStatesDesktopNavigationTreeId);
        when(siloNavTreeRepository.getObject(unitedStatesDesktopNavigationTreeId)).thenReturn(siloNavTree.toString());

        when()
                .get("/navigation/silos?countryCode=US")
                .then().statusCode(200)
                .body("silos.name", hasItems("some-display-name"))
                .body("silos.url", hasItems("/some/url"));
    }

    @Test
    public void shouldServeDesktopSiloDataFromRepository_whenCountryCodeParamIsNotSet() {

        SiloNavTree siloNavTree = createNavigationTree(unitedStatesDesktopNavigationTreeId);
        when(siloNavTreeRepository.getObject(unitedStatesDesktopNavigationTreeId)).thenReturn(siloNavTree.toString());

        when()
                .get("/navigation/silos?deviceType=desktop")
                .then().statusCode(200)
                .body("silos.name", hasItems("some-display-name"))
                .body("silos.url", hasItems("/some/url"));
    }

    @Test
    public void shouldServeMobileSiloDataFromRepository_whenDeviceTypeParamIsSetToMobile() {

        SiloNavTree siloNavTree = createNavigationTree(unitedStatesMobileNavigationTreeId);
        when(siloNavTreeRepository.getObject(unitedStatesMobileNavigationTreeId)).thenReturn(siloNavTree.toString());

        when()
                .get("/navigation/silos?deviceType=mobile&countryCode=US")
                .then().statusCode(200)
                .body("silos.name", hasItems("some-display-name"))
                .body("silos.url", hasItems("/some/url"));
    }

    @Test
    public void shouldServeDesktopSiloDataFromRepository_whenDeviceTypeParamIsSetToDesktop() {

        SiloNavTree siloNavTree = createNavigationTree(unitedStatesDesktopNavigationTreeId);
        when(siloNavTreeRepository.getObject(unitedStatesDesktopNavigationTreeId)).thenReturn(siloNavTree.toString());

        when()
                .get("/navigation/silos?deviceType=desktop&countryCode=US")
                .then().statusCode(200)
                .body("silos.name", hasItems("some-display-name"))
                .body("silos.url", hasItems("/some/url"));
    }

    @Test
    @SneakyThrows
    public void shouldRespond500_whenBadNavigationTreeDataRetrievedFromRepo() {

        SiloNavTree siloNavTree = new SiloNavTree(unitedStatesDesktopNavigationTreeId, new JSONObject().put("notARealProperty", "blahblahblah").toString());
        when(siloNavTreeRepository.getObject(unitedStatesDesktopNavigationTreeId)).thenReturn(siloNavTree.toString());

        when()
                .get("/navigation/silos")
                .then()
                .statusCode(500)
                .body("message", containsString("Unrecognized field \"notARealProperty\""))
                .body("statusCode", is(500));
    }
}

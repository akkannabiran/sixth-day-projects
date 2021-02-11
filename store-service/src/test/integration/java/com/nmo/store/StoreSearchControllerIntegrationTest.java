package com.sixthday.store;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.ValidatableResponse;
import com.sixthday.store.toggles.Features;
import com.toggler.core.utils.FeatureToggleRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.netflix.hystrix.HystrixHealthIndicator;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {StoreLocatorServiceApplication.class},
    properties = {"spring.cache.type=none", "spring.cloud.stream.bindings.output.binder=test", "sixthday-store-sub.products-foundation-mode=false","spring.cloud.stream.bindings.storeInventoryBySKU.binder=test2"},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings("all")
public class StoreSearchControllerIntegrationTest {

    private static final String STORE_SERVICE_URL = "/stores";
    private static final int DELAY_MILLIS = 50;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private HystrixHealthIndicator hystrixHealthIndicator;

    @Value("${local.server.port}")
    private int port;

    @Rule
    public FeatureToggleRepository featureToggleRepository = new FeatureToggleRepository();

    @Before
    public void setUp() {
        RestAssured.port = port;
        featureToggleRepository.enable(Features.STUB_GOT_WWW);
    }

    @Test
    public void shouldOpenHystrixCircuitWhenMultipleCallsAreMadeToGotWWWAndItsUnavailable() {
        Mockito.when(restTemplate.getForEntity(any(), eq(String.class))).thenThrow(new HttpClientErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        assertThat(hystrixHealthIndicator.health().getStatus(), is(Status.UP));

        generateMultipleStoreSearchRequests();

        assertThat(hystrixHealthIndicator.health().getStatus(), is(new Status("CIRCUIT_OPEN")));
    }
    
    @Test
    public void shouldReturnStatusCode424WhenTimeoutExceptionThrown() {
    	Mockito.when(restTemplate.getForEntity(any(), eq(String.class))).thenThrow(new RuntimeException(new TimeoutException("GotLocations timed out.")));
    	
    	ValidatableResponse response = given()
    			.queryParam("brandCode", "nm")
    			.queryParam("freeFormAddress", "dallas")
    			.queryParam("skuId", "skuId")
    			.queryParam("quantity", 1)
    			.when()
    			.get(STORE_SERVICE_URL)
    			.then();
    	
    	response.statusCode(424);
    }

    private void generateMultipleStoreSearchRequests() {
        IntStream.range(1, 20).forEach(i -> {
            given()
                    .queryParam("brandCode", "nm")
                    .queryParam("freeFormAddress", "dallas")
                    .queryParam("skuId", "skuId")
                    .queryParam("quantity", 1)
                    .when()
                    .get(STORE_SERVICE_URL)
                    .then();
            waitFor(DELAY_MILLIS);
        });
    }

    private void waitFor(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception ignored) {
        }
    }
}

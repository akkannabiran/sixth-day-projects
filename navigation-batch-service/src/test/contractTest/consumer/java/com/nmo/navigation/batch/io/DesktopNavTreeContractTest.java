package com.sixthday.navigation.batch.io;

import au.com.dius.pact.consumer.ConsumerPactTestMk2;
import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import com.sixthday.navigation.batch.vo.SiloNavTreeReaderResponse;
import com.sixthday.navigation.exceptions.NavigationBatchServiceException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class DesktopNavTreeContractTest extends ConsumerPactTestMk2 {

    @Override
    protected RequestResponsePact createPact(PactDslWithProvider builder) {
        return builder
                .given("desktop silos don't exist") // NOTE: Using provider states are optional, you can leave it out
                .uponReceiving("when no desktop silos exist for a country code")
                .path("/desktop-navigation/silos")
                .query("countryCode=BQ")
                .method("GET")
                .willRespondWith()
                .status(404)
                .body("{}")
                .given("desktop silos exist") // NOTE: Using provider states are optional, you can leave it out
                .uponReceiving("when desktop silos exist for a countrycode")
                .method("GET")
                .path("/desktop-navigation/silos")
                .query("countryCode=IN")
                .willRespondWith()
                .status(200)
                .body(responseWhenSilosExist())
                .toPact();
    }

    private String responseWhenSilosExist() {
        StringBuilder builder = new StringBuilder();
        builder.append("{status: \"SUCCESS\",");
        builder.append("silos: [\n" +
                "{\n" +
                "id: \"cat000730\",\n" +
                "siloDisplayName: \"Designers\",\n" +
                "url: \"/Designers/cat000730/c.cat\",\n" +
                "attributes: {\n" +
                "promoPath: \"/category/cat45050736/r_main_drawer_promo.html\"\n" +
                "},\n" +
                "columns: [\n" +
                "{\n" +
                "level1Categories: [\n" +
                "{}");
        builder.append("]\n" +
                "}\n" +
                "]\n" +
                "}\n" +
                "]\n" +
                "}");
        return builder.toString();
    }

    @Override
    protected String providerName() {
        return "ATG-service";
    }

    @Override
    protected String consumerName() {
        return "navigation-batch-service-desktop-nav";
    }

    @Override
    protected void runTest(MockServer mockServer) throws IOException {
        SiloNavTreeReader reader = new SiloNavTreeReader(mockServer.getUrl() + "/desktop-navigation/silos?countryCode={country_code}", "IN", "abc", null, new RestTemplate());
        SiloNavTreeReaderResponse readerResponse = reader.read();
        assertThat(readerResponse.getCountryCode(), is("IN"));
        assertNotNull(readerResponse.getNavTree());

        reader = new SiloNavTreeReader(mockServer.getUrl() + "/desktop-navigation/silos?countryCode={country_code}", "BQ", "abc", null, new RestTemplate());
        boolean exceptionRaised = false;
        try {
            reader.read();
        } catch (NavigationBatchServiceException e) {
            exceptionRaised = true;
        }
        assertTrue(exceptionRaised);
    }
}

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

public class MobileNavTreeContractTest extends ConsumerPactTestMk2 {

    @Override
    protected RequestResponsePact createPact(PactDslWithProvider builder) {
        return builder
                .given("mobile silos don't exist") // NOTE: Using provider states are optional, you can leave it out
                .uponReceiving("when no mobile silos exist for a country code")
                .path("/navigation")
                .query("countryCode=BQ")
                .method("GET")
                .willRespondWith()
                .status(404)
                .body("{}")
                .given("mobile silos exist") // NOTE: Using provider states are optional, you can leave it out
                .uponReceiving("when mobile silos exist for a countrycode")
                .method("GET")
                .path("/navigation")
                .query("countryCode=IN")
                .willRespondWith()
                .status(200)
                .body(responseWhenSilosExist())
                .toPact();
    }

    private String responseWhenSilosExist() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n" +
                "id: \"cat000000\",\n" +
                "displayName: \"\",\n" +
                "url: \"/Neimans-Store-Catalog/cat000000/c.cat\",\n" +
                "tags: [\n" +
                "\"HC\"\n" +
                "],\n" +
                "children: [");
        builder.append("{\n" +
                "id: \"cat42120742\",\n" +
                "displayName: \"All Designers\",\n" +
                "url: \"/Jewelry-Accessories/All-Designers/cat42120742_cat4870731_cat000000/c.cat\",\n" +
                "tags: [\n" +
                "\"HC\"\n" +
                "],\n" +
                "children: []}");
        builder.append("]}");

        return builder.toString();
    }

    @Override
    protected String providerName() {
        return "ATG-service";
    }

    @Override
    protected String consumerName() {
        return "navigation-batch-service-mobile-nav";
    }

    @Override
    protected void runTest(MockServer mockServer) throws IOException {
        SiloNavTreeReader reader = new SiloNavTreeReader(mockServer.getUrl() + "/navigation?countryCode={country_code}", "IN", "abc", null, new RestTemplate());
        SiloNavTreeReaderResponse readerResponse = reader.read();
        assertThat(readerResponse.getCountryCode(), is("IN"));
        assertNotNull(readerResponse.getNavTree());

        reader = new SiloNavTreeReader(mockServer.getUrl() + "/navigation?countryCode={country_code}", "BQ", "abc", null, new RestTemplate());
        boolean exceptionRaised = false;
        try {
            reader.read();
        } catch (NavigationBatchServiceException e) {
            exceptionRaised = true;
        }
        assertTrue(exceptionRaised);
    }
}

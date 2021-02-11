package com.sixthday.navigation.integration.config;

import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.monitoring.ClientEventHandler;
import com.orbitz.consul.util.Http;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Header;
import org.mockserver.model.HttpResponse;
import org.springframework.cloud.consul.ConsulProperties;

import java.lang.reflect.Field;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.StringBody.json;

@RunWith(MockitoJUnitRunner.class)
public class ConsulClientConfigurationTest {

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(12001, this);

    @Mock
    private ConsulProperties consulProperties;

    @InjectMocks
    private ConsulClientConfiguration consulClientConfiguration;

    @Test
    public void shouldCreateKeyValueClient() throws URISyntaxException, NoSuchFieldException, IllegalAccessException {
        String expectedClient = "keyvalue";

        when(consulProperties.getHost()).thenReturn("localhost");
        when(consulProperties.getPort()).thenReturn(12001);
        new MockServerClient("localhost", 12001).when(request().withMethod("GET")).respond(HttpResponse.response().withStatusCode(200).
                withHeaders(Header.header("Content-Length", 212),
                        Header.header("Content-Type", "application/json"),
                        Header.header("X-Consul-Index", 755),
                        Header.header("X-Consul-Knownleader", "true")).
                withBody(json("[\n" +
                        "    {\n" +
                        "        \"LockIndex\": 0,\n" +
                        "        \"Key\": \"environments\",\n" +
                        "        \"Flags\": 0,\n" +
                        "        \"Value\": \"dHJ1ZQ==\",\n" +
                        "        \"CreateIndex\": 1444,\n" +
                        "        \"ModifyIndex\": 1444\n" +
                        "    },\n" +
                        "    {\n" +
                        "        \"LockIndex\": 0,\n" +
                        "        \"Key\": \"environments/dev-int/config/data-services-enabled\",\n" +
                        "        \"Flags\": 0,\n" +
                        "        \"Value\": \"dHJ1ZQ==\",\n" +
                        "        \"CreateIndex\": 717,\n" +
                        "        \"ModifyIndex\": 1346\n" +
                        "    }\n" +
                        "]")));

        KeyValueClient keyValueClient = consulClientConfiguration.consulKeyValueClient();
        Field httpField = keyValueClient.getClass().getSuperclass().getDeclaredField("http");
        httpField.setAccessible(true);
        Http httpClient = (Http) httpField.get(keyValueClient);

        Field eventHandlerField = httpClient.getClass().getDeclaredField("eventHandler");
        eventHandlerField.setAccessible(true);
        ClientEventHandler clientEventHandler = (ClientEventHandler) eventHandlerField.get(httpClient);

        Field clientNameField = clientEventHandler.getClass().getDeclaredField("clientName");
        clientNameField.setAccessible(true);
        String clientName = (String) clientNameField.get(clientEventHandler);

        assertEquals(expectedClient, clientName);
    }
}
package com.sixthday.navigation.listeners;

import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.cache.KVCache;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.utils.URIBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Header;
import org.mockserver.model.HttpResponse;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.StringBody.json;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.class)
@PowerMockIgnore({"javax.net.ssl.*", "javax.management.*"})
@PrepareForTest({KVCache.class})
public class ConsulClientListenerTest {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();
    @Rule
    public MockServerRule mockServerRule = new MockServerRule(12000, this);
    @Mock
    private SimpleMessageListenerContainer listenerContainer;

    @Mock
    private ContextRefreshedEvent event;

    private KeyValueClient getConsulKeyValueClient() throws URISyntaxException {
        URI uri = new URIBuilder().setScheme("http").setHost("localhost").setPort(12000).build();
        Consul consul = Consul.builder().withUrl(uri.toString()).build();
        return consul.keyValueClient();
    }

    private void mockConsulForGetRequestFromConsulClient(String returnValue, String uniqueEnvironmentName) {
        String body = "[\n" +
                "    {\n" +
                "        \"LockIndex\": 0,\n" +
                "        \"Key\": \"environments\",\n" +
                "        \"Flags\": 0,\n" +
                "        \"Value\": \"" + returnValue + "\",\n" +
                "        \"CreateIndex\": 1444,\n" +
                "        \"ModifyIndex\": 1444\n" +
                "    },\n" +
                "    {\n" +
                "        \"LockIndex\": 0,\n" +
                "        \"Key\": \"environments/" + uniqueEnvironmentName + "/config/data-services-enabled\",\n" +
                "        \"Flags\": 0,\n" +
                "        \"Value\": \"" + returnValue + "\",\n" +
                "        \"CreateIndex\": 717,\n" +
                "        \"ModifyIndex\": 1346\n" +
                "    }\n" +
                "]";

        new MockServerClient("localhost", 12000).when(request().withMethod("GET")).respond(HttpResponse.response().withStatusCode(200).
                withHeaders(Header.header("Content-Length", 212),
                        Header.header("Content-Type", "application/json"),
                        Header.header("X-Consul-Index", 755),
                        Header.header("X-Consul-Knownleader", "true")).
                withBody(json(body)));
    }

    @Test
    public void shouldNotAddListenerWhenAlreadyAdded() throws URISyntaxException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, InterruptedException {
        String uniqueEnvironmentName = "dev-int";
        int expectedNumberOfListeners = 1;
        mockConsulForGetRequestFromConsulClient(Base64.encodeBase64String("false".getBytes()), uniqueEnvironmentName);
        KeyValueClient keyValueClient = getConsulKeyValueClient();
        KVCache kvCacheSpied = spy(KVCache.newCache(keyValueClient, "environments"));
        kvCacheSpied.addListener(value -> {
        });
        mockStatic(KVCache.class);
        when(KVCache.newCache(keyValueClient, "environments")).thenReturn(kvCacheSpied);
        Class name = Class.forName("com.sixthday.navigation.listeners.ConsulClientListener");
        java.lang.reflect.Constructor ct = name.getDeclaredConstructor(KeyValueClient.class,
                SimpleMessageListenerContainer.class);
        ct.setAccessible(true);
        ConsulClientListener consulClientListener = (ConsulClientListener) ct.newInstance(keyValueClient,
                listenerContainer);
        consulClientListener.onApplicationEvent(event);
        assertEquals(expectedNumberOfListeners, kvCacheSpied.getListeners().size());
        verify(kvCacheSpied, times(0)).start();
    }

    @Test
    public void shouldAddListenerWhenNotAlreadyAdded() throws URISyntaxException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, InterruptedException {
        String uniqueEnvironmentName = "dev-int";
        int expectedNumberOfListeners = 1;
        mockConsulForGetRequestFromConsulClient(Base64.encodeBase64String("false".getBytes()), uniqueEnvironmentName);
        KeyValueClient keyValueClient = getConsulKeyValueClient();
        KVCache kvCacheSpied = spy(KVCache.newCache(keyValueClient, "environments"));
        mockStatic(KVCache.class);
        when(KVCache.newCache(keyValueClient, "environments")).thenReturn(kvCacheSpied);
        Class name = Class.forName("com.sixthday.navigation.listeners.ConsulClientListener");
        java.lang.reflect.Constructor ct = name.getDeclaredConstructor(KeyValueClient.class,
                SimpleMessageListenerContainer.class);
        ct.setAccessible(true);
        ConsulClientListener consulClientListener = (ConsulClientListener) ct.newInstance(keyValueClient,
                listenerContainer);
        consulClientListener.onApplicationEvent(event);
        assertEquals(expectedNumberOfListeners, kvCacheSpied.getListeners().size());
        verify(kvCacheSpied, times(1)).start();
    }

    @Test
    public void shouldStartStopConsumingMessageQueueBasedOnConsulConfig() throws URISyntaxException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, InterruptedException {
        String uniqueEnvironmentName = "dev-int";
        environmentVariables.set("ENV_UNIQUE_NAME", uniqueEnvironmentName);
        mockConsulForGetRequestFromConsulClient(Base64.encodeBase64String("true".getBytes()), uniqueEnvironmentName);
        KeyValueClient keyValueClient = getConsulKeyValueClient();
        Class name = Class.forName("com.sixthday.navigation.listeners.ConsulClientListener");
        java.lang.reflect.Constructor ct = name.getDeclaredConstructor(KeyValueClient.class, SimpleMessageListenerContainer.class);
        ct.setAccessible(true);
        ConsulClientListener consulClientListener = (ConsulClientListener) ct.newInstance(keyValueClient, listenerContainer);
        consulClientListener.onApplicationEvent(event);
        Thread.sleep(1000);
        verify(listenerContainer, times(1)).start();
        mockConsulForGetRequestFromConsulClient(Base64.encodeBase64String("false".getBytes()), uniqueEnvironmentName);
        Thread.sleep(1000);
        verify(listenerContainer, times(1)).stop();
    }

    @Test
    public void shouldStartConsumingMessageWhenEnvironmentVariableIsNotPresent() throws URISyntaxException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, InterruptedException {
        String uniqueEnvironmentName = "dev-int";
        mockConsulForGetRequestFromConsulClient(Base64.encodeBase64String("false".getBytes()), uniqueEnvironmentName);
        KeyValueClient keyValueClient = getConsulKeyValueClient();
        Class name = Class.forName("com.sixthday.navigation.listeners.ConsulClientListener");
        java.lang.reflect.Constructor ct = name.getDeclaredConstructor(KeyValueClient.class, SimpleMessageListenerContainer.class);
        ct.setAccessible(true);
        ConsulClientListener consulClientListener = (ConsulClientListener) ct.newInstance(keyValueClient, listenerContainer);
        consulClientListener.onApplicationEvent(event);
        Thread.sleep(1000);
        verify(listenerContainer, times(1)).start();
        verify(listenerContainer, times(0)).stop();
    }

    @Test
    public void shouldConsumeMessageQueueWhenNoConfigIsPresent() throws URISyntaxException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, InterruptedException {
        new MockServerClient("localhost", 12000).when(request().withMethod("GET")).respond(HttpResponse.response().withStatusCode(200).
                withHeaders(Header.header("Content-Length", 212),
                        Header.header("Content-Type", "application/json"),
                        Header.header("X-Consul-Index", 755),
                        Header.header("X-Consul-Knownleader", "true")).
                withBody(json("[\n" +
                        "    {\n" +
                        "        \"LockIndex\": 0,\n" +
                        "        \"Key\": \"environments/default/config/data-services-enabled\",\n" +
                        "        \"Flags\": 0,\n" +
                        "        \"Value\": \"dHJ1ZQ==\",\n" +
                        "        \"CreateIndex\": 2199,\n" +
                        "        \"ModifyIndex\": 2212\n" +
                        "    }\n" +
                        "]")));

        KeyValueClient keyValueClient = getConsulKeyValueClient();
        Class name = Class.forName("com.sixthday.navigation.listeners.ConsulClientListener");
        java.lang.reflect.Constructor ct = name.getDeclaredConstructor(KeyValueClient.class, SimpleMessageListenerContainer.class);
        ct.setAccessible(true);
        ConsulClientListener consulClientListener = (ConsulClientListener) ct.newInstance(keyValueClient, listenerContainer);
        consulClientListener.onApplicationEvent(event);
        Thread.sleep(1000);
        verify(listenerContainer, times(1)).start();
        verify(listenerContainer, times(0)).stop();
    }

}
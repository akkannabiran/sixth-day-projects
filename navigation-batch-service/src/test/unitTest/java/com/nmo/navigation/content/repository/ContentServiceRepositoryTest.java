package com.sixthday.navigation.content.repository;

import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URISyntaxException;
import java.util.*;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ContentServiceRepositoryTest {
    private final static String SCHEME = "http";
    private final static String SILODRAWERASSETURL = "/silodrawerasseturl";
    private static String HOST = "some-host";
    private static int PORT = 8800;
    @Mock
    RestTemplate restTemplate;
    @Mock
    private NavigationBatchServiceConfig navigationBatchServiceConfig;
    @Mock
    private NavigationBatchServiceConfig.IntegrationConfig integrationConfig;
    @Mock
    private NavigationBatchServiceConfig.ServiceConfig contentServiceConfig;
    @InjectMocks
    private ContentServiceRepository contentServiceRepository;

    @Before
    public void setUp() throws Exception {
        when(navigationBatchServiceConfig.getIntegration()).thenReturn(integrationConfig);
        when(integrationConfig.getContentServiceConfig()).thenReturn(contentServiceConfig);
        when(contentServiceConfig.getScheme()).thenReturn(SCHEME);
        when(contentServiceConfig.getHost()).thenReturn(HOST);
        when(contentServiceConfig.getPort()).thenReturn(PORT);
        when(contentServiceConfig.getServiceUrl()).thenReturn(SILODRAWERASSETURL);
        when(contentServiceConfig.isEnabled()).thenReturn(true);
    }

    @Test
    public void shouldReturnSiloDrawerAssetMapFromAEMForGivenSiloIfAssetPresent() throws Exception {
        LinkedHashMap<String, Object> mockResponse = new LinkedHashMap<>();
        mockResponse.put("categoryId", "cat000001");
        mockResponse.put("layoutType", "complex");
        Object rows = new Object();
        mockResponse.put("rows", rows);
        List list = new ArrayList();
        list.add(mockResponse);
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("cat000001", mockResponse);
        ResponseEntity responseEntity = ResponseEntity.ok().body(list);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), Matchers.any(), eq(ArrayList.class))).thenReturn(responseEntity);

        Map<String, Object> assetsFromAEM = contentServiceRepository.getSiloDrawerAsset(Arrays.asList("cat000001"));

        assertThat(assetsFromAEM, is(expectedResponse));
    }

    @Test
    public void shouldReturnEmptySiloDrawerAssetMapFromAEMForGivenSiloIfAssetNotPresent() throws Exception {
        List list = new ArrayList();
        Map<String, Object> expectedResponse = new HashMap<>();
        ResponseEntity responseEntity = ResponseEntity.ok().body(list);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), Matchers.any(), eq(ArrayList.class))).thenReturn(responseEntity);

        Map<String, Object> assetsFromAEM = contentServiceRepository.getSiloDrawerAsset(Arrays.asList("cat000001"));

        assertThat(assetsFromAEM, is(expectedResponse));
        assertThat(assetsFromAEM.size(), is(0));
    }

    @Test
    public void shouldReturnEmptySiloDrawerAssetMapFromAEMForGivenSiloIfContentServiceCallDisabled() throws Exception {
        when(contentServiceConfig.isEnabled()).thenReturn(false);
        List list = new ArrayList();
        Map<String, Object> expectedResponse = new HashMap<>();
        ResponseEntity responseEntity = ResponseEntity.ok().body(list);

        Map<String, Object> assetsFromAEM = contentServiceRepository.getSiloDrawerAsset(Arrays.asList("cat000001"));

        assertThat(assetsFromAEM, is(expectedResponse));
        assertThat(assetsFromAEM.size(), is(0));
    }

    @Test(expected = URISyntaxException.class)
    public void shouldReturnEmptySiloDrawerAssetMapIfContentServiceConfigurationIncorrect() throws Exception {
        when(contentServiceConfig.getScheme()).thenReturn("http$$");
        when(contentServiceConfig.getHost()).thenReturn(HOST);
        when(contentServiceConfig.getPort()).thenReturn(PORT);

        List list = new ArrayList();
        Map<String, Object> expectedResponse = new HashMap<>();
        ResponseEntity responseEntity = ResponseEntity.ok().body(list);

        Map<String, Object> assetsFromAEM = contentServiceRepository.getSiloDrawerAsset(Arrays.asList("cat000001"));
    }
}
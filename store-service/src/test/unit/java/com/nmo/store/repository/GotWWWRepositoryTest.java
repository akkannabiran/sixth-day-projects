package com.sixthday.store.repository;

import com.sixthday.store.config.RealGotWWWConfig;
import com.sixthday.store.data.GotWWWResponseBuilder;
import com.sixthday.store.exceptions.GotWWWCommunicationException;
import com.sixthday.store.exceptions.GotWWWResponseParseException;
import com.sixthday.store.exceptions.InvalidLocationException;
import com.sixthday.store.models.Coordinates;
import com.sixthday.store.models.StoreSearchLocation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GotWWWRepositoryTest {

    @Spy
    private RealGotWWWConfig config;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private StoreSearchLocation storeSearchLocation;

    @InjectMocks
    private GotWWWRepository repository;

    private GotWWWResponseBuilder gotWWWResponseBuilder;

    @Before
    public void setUp() throws Exception {
        config.setHost("host.com");
        config.setScheme("http");
        config.setDefaultMileRadius(100);
        config.setResultLimit(99999);
        gotWWWResponseBuilder = new GotWWWResponseBuilder();
    }

    @Test(expected = GotWWWCommunicationException.class)
    public void shouldThrowGotWWWCommunicationExceptionWhenRestClientErrorOccurs() throws Exception {
        when(storeSearchLocation.getFreeFormAddress()).thenReturn("some address");
        when(restTemplate.getForEntity(any(URI.class), eq(String.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        repository.getStores("BRAND_CODE", storeSearchLocation, Optional.of(50));
    }

    @Test
    public void shouldReturnNoMatchesStoreResponseIfReturnedStoreListIsNull() throws Exception {
        ResponseEntity<String> build = ResponseEntity.ok(
                gotWWWResponseBuilder.withStatusCodeDescription("1", "no matches")
                        .withLongitudeLatitude("1", "1")
                        .build()
        );
        when(storeSearchLocation.getCoordinates()).thenReturn(new Coordinates(1f, 1f));

        when(restTemplate.getForEntity(any(), eq(String.class))).thenReturn(build);

        List<String> stores = repository.getStores("BRAND_CODE", storeSearchLocation, Optional.of(100));

        assertThat(stores, is(Collections.emptyList()));
    }

    @Test(expected = InvalidLocationException.class)
    public void shouldThrowExceptionWhenInvalidLocationReturned() throws Exception {
        ResponseEntity<String> build = ResponseEntity.ok(
                gotWWWResponseBuilder.withStatusCodeDescription("1", "no matches")
                        .withLongitudeLatitude("", "")
                        .build()
        );

        when(storeSearchLocation.getFreeFormAddress()).thenReturn("some address");
        when(restTemplate.getForEntity(any(), eq(String.class))).thenReturn(build);

        repository.getStores("BRAND_CODE", storeSearchLocation, Optional.of(100));
    }

    @Test(expected = GotWWWResponseParseException.class)
    public void shouldThrowExceptionWhenInvalidResponseReturned() throws Exception {
        ResponseEntity<String> build = ResponseEntity.ok("Invalid Response");
        when(storeSearchLocation.getFreeFormAddress()).thenReturn("some address");

        when(restTemplate.getForEntity(any(), eq(String.class))).thenReturn(build);

        repository.getStores("BRAND_CODE", storeSearchLocation, Optional.of(100));
    }

    @Test
    public void shouldPickMinResultsAndDefaultRadiusFromConfigWhenNotGiven() throws Exception {
        ResponseEntity<String> build = ResponseEntity.ok(
                gotWWWResponseBuilder.withStatusCodeDescription("0", "Success")
                        .withLongitudeLatitude("1", "1")
                        .build()
        );
        when(storeSearchLocation.getFreeFormAddress()).thenReturn("some address");

        ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
        when(restTemplate.getForEntity(any(), eq(String.class))).thenReturn(build);

        repository.getStores("BRAND_CODE", storeSearchLocation, Optional.empty());

        Mockito.verify(restTemplate).getForEntity(uri.capture(), eq(String.class));
        String requestUrl = uri.getValue().toString();
        assertEquals(requestUrl.contains("mile_radius=100"), true);
        assertEquals(requestUrl.contains("min_results=99999"), true);
    }

    @Test
    public void shouldReturnListOfStoresForAddress() throws Exception {
        ResponseEntity<String> build = ResponseEntity.ok(
                gotWWWResponseBuilder.withStatusCodeDescription("0", "Success")
                        .withLongitudeLatitude("1", "1")
                        .withNoOfStores(1)
                        .build()
        );

        when(storeSearchLocation.getFreeFormAddress()).thenReturn("some address");

        when(restTemplate.getForEntity(any(), eq(String.class))).thenReturn(build);

        List<String> stores = repository.getStores("BRAND_CODE", storeSearchLocation, Optional.of(100));

        assertThat(stores, containsInAnyOrder("1"));
    }

    @Test
    public void shouldReturnListOfStoresForLatitudeLongitude() throws Exception {
        ResponseEntity<String> build = ResponseEntity.ok(
                gotWWWResponseBuilder.withStatusCodeDescription("0", "Success")
                        .withLongitudeLatitude("1", "1")
                        .withNoOfStores(1)
                        .build()
        );

        when(storeSearchLocation.getCoordinates()).thenReturn(new Coordinates(1f, 1f));

        when(restTemplate.getForEntity(any(), eq(String.class))).thenReturn(build);

        List<String> stores = repository.getStores("BRAND_CODE", storeSearchLocation, Optional.of(100));

        assertThat(stores, containsInAnyOrder("1"));
    }
}

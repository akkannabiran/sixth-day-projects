package com.sixthday.navigation.batch.io;

import com.sixthday.navigation.batch.vo.SiloNavTreeReaderResponse;
import com.sixthday.navigation.exceptions.NavigationBatchServiceException;
import org.junit.Test;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static com.sixthday.testing.ExceptionThrower.assertThrown;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class SiloNavTreeReaderTest {

    private static final String ENDPOINT = "http://endpoint";

    private SiloNavTreeReader siloNavTreeReader = spy(new SiloNavTreeReader(ENDPOINT, "US", "userAgent", null, new RestTemplate()));

    @Test
    public void shouldReturnNavTreeNodeData_whenResponseEntityHttpStatusIsOk() {
        final SiloNavTreeReaderResponse actualNavTreeNodeData = new SiloNavTreeReaderResponse("US", "some desktop nav tree node data", null);

        doReturn(new ResponseEntity<>(actualNavTreeNodeData.getNavTree(), HttpStatus.OK)).when(siloNavTreeReader).callNavTreeAPI();

        SiloNavTreeReaderResponse expectedNavTreeNodeData = siloNavTreeReader.read();
        assertThat(actualNavTreeNodeData.getNavTree(), is(expectedNavTreeNodeData.getNavTree()));
    }

    @Test
    public void shouldThrowException_whenResponseEntityHttpStatusIsOtherThanOk() {
        doReturn(new ResponseEntity<>(HttpStatus.ACCEPTED)).when(siloNavTreeReader).callNavTreeAPI();

        Throwable thrown = assertThrown(() -> siloNavTreeReader.read());

        assertThat("Failure exception class", thrown, instanceOf(NavigationBatchServiceException.class));
        assertThat("Failure exception message", thrown.getMessage(), containsString("Status Code"));
    }

    @Test
    public void shouldEscapeHtmlEncodedChars() {
        final String actualNavTreeNodeData = "&egrave;";
        doReturn(new ResponseEntity<>(actualNavTreeNodeData, HttpStatus.OK)).when(siloNavTreeReader).callNavTreeAPI();

        SiloNavTreeReaderResponse expectedNavTreeNodeData = siloNavTreeReader.read();

        assertThat(expectedNavTreeNodeData.getNavTree(), is("è"));
    }

    @Test
    public void shouldThrowResourceAccessException_whenThereIsADelayInATGCalls() {
        doThrow(new ResourceAccessException("Exception")).when(siloNavTreeReader).callNavTreeAPI();

        Throwable thrown = assertThrown(() -> siloNavTreeReader.read());

        assertThat("Failure exception class", thrown, instanceOf(NavigationBatchServiceException.class));
        assertThat("Failure exception message", thrown.getMessage(), containsString("Time-out"));
    }

    @Test
    public void shouldUseCountryCodeAndNavKeyGroupToFetchNavTreeNodeDataWhenCountryCodeAndNavKeyGroupIsConfiguerd() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        SiloNavTreeReader reader = new SiloNavTreeReader("http://endpoint?c={country_code}&g={navKeyGroup}", "US", "SomeValue", "B", mockRestTemplate);
        SiloNavTreeReaderResponse expectedNavTreeNodeData = new SiloNavTreeReaderResponse("US", "some desktop nav tree node data", "B");
        doReturn(new ResponseEntity<>(expectedNavTreeNodeData.getNavTree(), HttpStatus.OK)).when(mockRestTemplate).exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class));

        SiloNavTreeReaderResponse actualNavTreeNodeData = reader.read();
        assertThat(actualNavTreeNodeData.getNavTree(), is(expectedNavTreeNodeData.getNavTree()));
        assertThat(actualNavTreeNodeData.getNavKeyGroup(), is(expectedNavTreeNodeData.getNavKeyGroup()));
        verify(mockRestTemplate).exchange(eq("http://endpoint?c=US&g=B"), eq(HttpMethod.GET), any(), eq(String.class));
    }

    @Test
    public void shouldUseCountryCodeAndEmptyNavKeyGroupToFetchNavTreeNodeDataWhenCountryCodeConfiguredAndNavKeyGroupIsEmpty() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        SiloNavTreeReader reader = new SiloNavTreeReader("http://endpoint?c={country_code}&g={navKeyGroup}", "US", "SomeValue", "", mockRestTemplate);
        SiloNavTreeReaderResponse expectedNavTreeNodeData = new SiloNavTreeReaderResponse("US", "some desktop nav tree node data", "");
        doReturn(new ResponseEntity<>(expectedNavTreeNodeData.getNavTree(), HttpStatus.OK)).when(mockRestTemplate).exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class));

        SiloNavTreeReaderResponse actualNavTreeNodeData = reader.read();
        assertThat(actualNavTreeNodeData.getNavTree(), is(expectedNavTreeNodeData.getNavTree()));
        assertThat(actualNavTreeNodeData.getNavKeyGroup(), is(expectedNavTreeNodeData.getNavKeyGroup()));
        verify(mockRestTemplate).exchange(eq("http://endpoint?c=US&g="), eq(HttpMethod.GET), any(), eq(String.class));
    }

    @Test
    public void shouldUseCountryCodeAndControlNavKeyGroupToFetchNavTreeNodeDataWhenCountryCodeConfiguredAndNavKeyGroupIsNull() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        SiloNavTreeReader reader = new SiloNavTreeReader("http://endpoint?c={country_code}&g={navKeyGroup}", "US", "SomeValue", null, mockRestTemplate);
        SiloNavTreeReaderResponse expectedNavTreeNodeData = new SiloNavTreeReaderResponse("US", "some desktop nav tree node data", null);
        doReturn(new ResponseEntity<>(expectedNavTreeNodeData.getNavTree(), HttpStatus.OK)).when(mockRestTemplate).exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class));

        SiloNavTreeReaderResponse actualNavTreeNodeData = reader.read();
        assertThat(actualNavTreeNodeData.getNavTree(), is(expectedNavTreeNodeData.getNavTree()));
        assertThat(actualNavTreeNodeData.getNavKeyGroup(), is(expectedNavTreeNodeData.getNavKeyGroup()));
        verify(mockRestTemplate).exchange(eq("http://endpoint?c=US&g=A"), eq(HttpMethod.GET), any(), eq(String.class));
    }

}

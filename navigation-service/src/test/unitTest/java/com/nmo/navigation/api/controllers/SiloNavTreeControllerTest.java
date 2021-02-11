package com.sixthday.navigation.api.controllers;

import com.sixthday.navigation.api.models.NavigationErrorResponse;
import com.sixthday.navigation.api.models.Silos;
import com.sixthday.navigation.api.services.SiloNavTreeService;
import com.sixthday.navigation.exceptions.SiloNavTreeNotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.MDC;

import static com.sixthday.navigation.api.data.SiloNavTreeTestDataFactory.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MDC.class)
public class SiloNavTreeControllerTest {

    private static final String UNITED_STATES_COUNTRY_CODE = "US";
    @Mock
    private SiloNavTreeService siloNavTreeService;
    @InjectMocks
    private SiloNavTreeController siloNavTreeController;

    @Test
    public void shouldReturnSilosForDeviceTypeDesktopAndCountryCode() {
        when(siloNavTreeService.getSilos(UNITED_STATES_COUNTRY_CODE, DEVICE_TYPE_DESKTOP, null)).thenReturn(getTestSilosForDesktop());

        Silos outputSilos = siloNavTreeController.getSilos(UNITED_STATES_COUNTRY_CODE, DEVICE_TYPE_DESKTOP, null);

        assertThat(outputSilos.getSilosTree().size(), is(2));
    }

    @Test
    public void shouldReturnSilosForDeviceTypeMobileAndCountryCode() {
        when(siloNavTreeService.getSilos(UNITED_STATES_COUNTRY_CODE, DEVICE_TYPE_MOBILE, null)).thenReturn(getTestSilosForMobile());

        Silos outputSilos = siloNavTreeController.getSilos(UNITED_STATES_COUNTRY_CODE, DEVICE_TYPE_MOBILE, null);

        assertThat(outputSilos.getSilosTree().size(), is(4));
    }

    @Test
    public void shouldReturnInitialSilosForMobileByCountryCode() {
        when(siloNavTreeService.getInitialMobileSilos(UNITED_STATES_COUNTRY_CODE, null)).thenReturn(getInitialMobileTestSilos());

        Silos outputSilos = siloNavTreeController.getInitialMobileSilos(UNITED_STATES_COUNTRY_CODE, null);

        assertThat(outputSilos.getSilosTree().size(), is(3));
    }

    @Test
    public void shouldServe404WhenSiloNavTreeNotFound() {
        NavigationErrorResponse navigationErrorResponse = siloNavTreeController.handleSiloNavTreeNotFoundException(new SiloNavTreeNotFoundException(EXCEPTION_MSG));

        assertThat(navigationErrorResponse.getMessage(), equalTo(EXCEPTION_MSG));

        assertThat(navigationErrorResponse.getStatusCode(), equalTo(404));
    }

    @Test(expected = NullPointerException.class)
    public void shouldServeNullPointerExceptionWhenCountryCodeIsNull() {
        siloNavTreeController.getInitialMobileSilos(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldServeNullPointerExceptionWhenCountryCodeAndDeviceTypeAreNull() {
        siloNavTreeController.getSilos(null, null, null);
    }
    
    @Test
    public void shouldReturnSilosForDeviceTypeDesktopCountryCodeUSAndNavKeyGroupIsNull() {
        when(siloNavTreeService.getSilos(UNITED_STATES_COUNTRY_CODE, DEVICE_TYPE_DESKTOP, null)).thenReturn(getTestSilosForDesktop());
        PowerMockito.mockStatic(MDC.class);

        Silos outputSilos = siloNavTreeController.getSilos(UNITED_STATES_COUNTRY_CODE, DEVICE_TYPE_DESKTOP, null);
        
        ArgumentCaptor<String> groupCaptor = ArgumentCaptor.forClass(String.class);
        verify(siloNavTreeService).getSilos(eq(UNITED_STATES_COUNTRY_CODE), eq(DEVICE_TYPE_DESKTOP), groupCaptor.capture());
        assertThat(groupCaptor.getValue(), nullValue());
        verifyStatic(never());
        MDC.put(eq("NavKeyGroup"), anyString());
        assertThat(outputSilos.getSilosTree().size(), is(2));
    }
    
    @Test
    public void shouldReturnSilosForDeviceTypeDesktopCountryCodeUSAndNavKeyGroupIsEmpty() {
        when(siloNavTreeService.getSilos(UNITED_STATES_COUNTRY_CODE, DEVICE_TYPE_DESKTOP, "")).thenReturn(getTestSilosForDesktop());
        PowerMockito.mockStatic(MDC.class);

        Silos outputSilos = siloNavTreeController.getSilos(UNITED_STATES_COUNTRY_CODE, DEVICE_TYPE_DESKTOP, "");
        
        ArgumentCaptor<String> groupCaptor = ArgumentCaptor.forClass(String.class);
        verify(siloNavTreeService).getSilos(eq(UNITED_STATES_COUNTRY_CODE), eq(DEVICE_TYPE_DESKTOP), groupCaptor.capture());
        assertThat(groupCaptor.getValue(), equalTo(""));
        verifyStatic(never());
        MDC.put(eq("NavKeyGroup"), anyString());
        assertThat(outputSilos.getSilosTree().size(), is(2));
    }
    
    @Test
    public void shouldReturnSilosForDeviceTypeDesktopCountryCodeUSAndNavKeyGroupA() {
        when(siloNavTreeService.getSilos(UNITED_STATES_COUNTRY_CODE, DEVICE_TYPE_DESKTOP, NAV_KEY_GROUP_CONTROL)).thenReturn(getTestSilosForDesktop());
        PowerMockito.mockStatic(MDC.class);

        Silos outputSilos = siloNavTreeController.getSilos(UNITED_STATES_COUNTRY_CODE, DEVICE_TYPE_DESKTOP, NAV_KEY_GROUP_CONTROL);

        ArgumentCaptor<String> groupCaptor = ArgumentCaptor.forClass(String.class);
        verify(siloNavTreeService).getSilos(eq(UNITED_STATES_COUNTRY_CODE), eq(DEVICE_TYPE_DESKTOP), groupCaptor.capture());
        assertThat(groupCaptor.getValue(), equalTo("A"));
        verifyStatic(times(1));
        MDC.put("NavKeyGroup", "A");
        assertThat(outputSilos.getSilosTree().size(), is(2));
    }
    
    @Test
    public void shouldReturnSilosForDeviceTypeDesktopCountryCodeUSAndNavKeyGroupB() {
      when(siloNavTreeService.getSilos(UNITED_STATES_COUNTRY_CODE, DEVICE_TYPE_DESKTOP, "B")).thenReturn(getTestSilosForDesktop());
      PowerMockito.mockStatic(MDC.class);

      Silos outputSilos = siloNavTreeController.getSilos(UNITED_STATES_COUNTRY_CODE, DEVICE_TYPE_DESKTOP, "B");

      ArgumentCaptor<String> groupCaptor = ArgumentCaptor.forClass(String.class);
      verify(siloNavTreeService).getSilos(eq(UNITED_STATES_COUNTRY_CODE), eq(DEVICE_TYPE_DESKTOP), groupCaptor.capture());
      assertThat(groupCaptor.getValue(), equalTo("B"));
      verifyStatic(times(1));
      MDC.put("NavKeyGroup", "B");
      assertThat(outputSilos.getSilosTree().size(), is(2));
    }
    
    @Test
    public void shouldReturnInitialSilosForDeviceTypeMobileCountryCodeUSAndNavKeyGroupIsNull() {
        PowerMockito.mockStatic(MDC.class);
        when(siloNavTreeService.getInitialMobileSilos(UNITED_STATES_COUNTRY_CODE, null)).thenReturn(getInitialMobileTestSilos());
        
        Silos outputSilos = siloNavTreeController.getInitialMobileSilos(UNITED_STATES_COUNTRY_CODE, null);
        
        ArgumentCaptor<String> groupCaptor = ArgumentCaptor.forClass(String.class);
        verify(siloNavTreeService).getInitialMobileSilos(eq(UNITED_STATES_COUNTRY_CODE), groupCaptor.capture());
        assertThat(groupCaptor.getValue(), equalTo(null));
        verifyStatic(never());
        MDC.put(eq("NavKeyGroup"), anyString());
        assertThat(outputSilos.getSilosTree().size(), is(3));
    }
    
    @Test
    public void shouldReturnInitialSilosForDeviceTypeMobileCountryCodeUSAndNavKeyGroupIsEmpty() {
      PowerMockito.mockStatic(MDC.class);
      when(siloNavTreeService.getInitialMobileSilos(UNITED_STATES_COUNTRY_CODE, "")).thenReturn(getInitialMobileTestSilos());
      
      Silos outputSilos = siloNavTreeController.getInitialMobileSilos(UNITED_STATES_COUNTRY_CODE, "");
      
      ArgumentCaptor<String> groupCaptor = ArgumentCaptor.forClass(String.class);
      verify(siloNavTreeService).getInitialMobileSilos(eq(UNITED_STATES_COUNTRY_CODE), groupCaptor.capture());
      assertThat(groupCaptor.getValue(), equalTo(""));
      verifyStatic(never());
      MDC.put(eq("NavKeyGroup"), anyString());
      assertThat(outputSilos.getSilosTree().size(), is(3));
    }
    
    @Test
    public void shouldReturnInitialSilosForDeviceTypeMobileCountryCodeUSAndNavKeyGroupA() {
      PowerMockito.mockStatic(MDC.class);
      when(siloNavTreeService.getInitialMobileSilos(UNITED_STATES_COUNTRY_CODE, "A")).thenReturn(getInitialMobileTestSilos());
      
      Silos outputSilos = siloNavTreeController.getInitialMobileSilos(UNITED_STATES_COUNTRY_CODE, "A");
      
      ArgumentCaptor<String> groupCaptor = ArgumentCaptor.forClass(String.class);
      verify(siloNavTreeService).getInitialMobileSilos(eq(UNITED_STATES_COUNTRY_CODE), groupCaptor.capture());
      assertThat(groupCaptor.getValue(), equalTo("A"));
      verifyStatic(times(1));
      MDC.put("NavKeyGroup", "A");
      assertThat(outputSilos.getSilosTree().size(), is(3));
    }
    
    @Test
    public void shouldReturnInitialSilosForDeviceTypeMobileCountryCodeUSAndNavKeyGroupB() {
      PowerMockito.mockStatic(MDC.class);
      when(siloNavTreeService.getInitialMobileSilos(UNITED_STATES_COUNTRY_CODE, "B")).thenReturn(getInitialMobileTestSilos());
      
      Silos outputSilos = siloNavTreeController.getInitialMobileSilos(UNITED_STATES_COUNTRY_CODE, "B");
      
      ArgumentCaptor<String> groupCaptor = ArgumentCaptor.forClass(String.class);
      verify(siloNavTreeService).getInitialMobileSilos(eq(UNITED_STATES_COUNTRY_CODE), groupCaptor.capture());
      assertThat(groupCaptor.getValue(), equalTo("B"));
      verifyStatic(times(1));
      MDC.put("NavKeyGroup", "B");
      assertThat(outputSilos.getSilosTree().size(), is(3));
    }
}

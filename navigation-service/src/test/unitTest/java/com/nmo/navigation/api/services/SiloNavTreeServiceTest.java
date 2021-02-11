package com.sixthday.navigation.api.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.api.models.Silos;
import com.sixthday.navigation.repository.S3SiloNavTreeReader;
import com.toggler.core.utils.FeatureToggleRepository;
import lombok.SneakyThrows;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static com.sixthday.navigation.api.data.SiloNavTreeTestDataFactory.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;

@RunWith(MockitoJUnitRunner.class)
public class SiloNavTreeServiceTest {
  
  @Rule
  public FeatureToggleRepository featureToggleRepository = new FeatureToggleRepository();
  @InjectMocks
  SiloNavTreeService siloNavTreeService;
  @Mock
  S3SiloNavTreeReader s3SiloNavTreeReader;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  NavigationServiceConfig navigationServiceConfig;
  
  @Test
  @SneakyThrows
  public void givenRowData_ReturnsMobileSiloData() {
    ObjectMapper mapper = new ObjectMapper();
    
    given(s3SiloNavTreeReader.loadSiloNavTree("US_mobile")).willReturn(mapper.writeValueAsString(createSilos()));
    given(navigationServiceConfig.getIntegration().getCountryCodes()).willReturn(Collections.singletonList("ABC_COUNTRY"));
    
    final Silos actualNavigationCategories = siloNavTreeService.getSilos(UNITED_STATES_COUNTRY_CODE, DEVICE_TYPE_MOBILE, null);
    
    String expectedNavigationCategoriesAsString = mapper.writeValueAsString(createSilos());
    String actualNavigationCategoriesAsString = mapper.writeValueAsString(actualNavigationCategories);
    
    assertThat(actualNavigationCategoriesAsString, is(expectedNavigationCategoriesAsString));
  }
  
  @Test
  @SneakyThrows
  public void givenRowData_ReturnsDesktopSiloData() {
    ObjectMapper mapper = new ObjectMapper();
    
    given(s3SiloNavTreeReader.loadSiloNavTree("US_desktop")).willReturn(mapper.writeValueAsString(createSilos()));
    given(navigationServiceConfig.getIntegration().getCountryCodes()).willReturn(Collections.singletonList("ABC_COUNTRY"));
    
    final Silos actualNavigationCategories = siloNavTreeService.getSilos(UNITED_STATES_COUNTRY_CODE, DEVICE_TYPE_DESKTOP, null);
    
    String expectedNavigationCategoriesAsString = mapper.writeValueAsString(createSilos());
    String actualNavigationCategoriesAsString = mapper.writeValueAsString(actualNavigationCategories);
    
    assertThat(actualNavigationCategoriesAsString, is(expectedNavigationCategoriesAsString));
  }
  
  @Test
  public void givenValidCountryCodeReturnsTheSameCountryCode() {
    given(navigationServiceConfig.getIntegration().getCountryCodes()).willReturn(Collections.singletonList("AD"));
    String actualCountryCode = "AD";
    String expectedCountryCode = siloNavTreeService.getResolvedCountryCode(actualCountryCode);
    assertThat(actualCountryCode, is(expectedCountryCode));
  }
  
  @Test
  public void givenInValidCountryCodeReturnsTheDefaultCountryCode() {
    given(navigationServiceConfig.getIntegration().getCountryCodes()).willReturn(Collections.singletonList("AD"));
    String actualCountryCode = "ZZ";
    String expectedCountryCode = siloNavTreeService.getResolvedCountryCode(actualCountryCode);
    assertThat("US", is(expectedCountryCode));
  }
  
  @Test
  public void givenCountryCodeDeviceTypeAndNullNavKeyGroupReturnsDefultNavKey() {
    given(navigationServiceConfig.getIntegration().getCountryCodes()).willReturn(Collections.singletonList("AD"));
    String expectedNavKey = "AD_device";
    String actualNavKey = siloNavTreeService.getNavKey("AD", "Device", null);
    assertThat(actualNavKey, is(expectedNavKey));
  }
  
  @Test
  public void givenCountryCodeDeviceTypeAndEmptyNavKeyGroupReturnsDefultNavKey() {
    given(navigationServiceConfig.getIntegration().getCountryCodes()).willReturn(Collections.singletonList("AD"));
    String expectedNavKey = "AD_device";
    String actualNavKey = siloNavTreeService.getNavKey("AD", "Device", "");
    assertThat(actualNavKey, is(expectedNavKey));
  }
  
  @Test
  public void givenCountryCodeDeviceTypeAndNavKeyControlGroupReturnsDefultNavKey() {
    given(navigationServiceConfig.getIntegration().getCountryCodes()).willReturn(Collections.singletonList("AD"));
    String expectedNavKey = "AD_device";
    String actualNavKey = siloNavTreeService.getNavKey("AD", "Device", "A");
    assertThat(actualNavKey, is(expectedNavKey));
  }
  
  @Test
  public void givenCountryCodeDeviceTypeAndNavKeyTestGroupReturnsDefultNavKey() {
    given(navigationServiceConfig.getIntegration().getCountryCodes()).willReturn(Collections.singletonList("AD"));
    String expectedNavKey = "AD_device_B";
    String actualNavKey = siloNavTreeService.getNavKey("AD", "Device", "B");
    assertThat(actualNavKey, is(expectedNavKey));
  }
  
  @Test(expected = Exception.class)
  public void throwsNavigationTreeNotFoundException_WhenNoNavData() {
    given(s3SiloNavTreeReader.loadSiloNavTree("dummy-id")).willReturn("displayName: some display name");
    siloNavTreeService.getSilos("dummy-country-code", "dummy-device-type", null);
  }
  
  @Test(expected = Exception.class)
  public void testGetSilosThrowException() {
    given(s3SiloNavTreeReader.loadSiloNavTree(anyString())).willThrow(Exception.class);
    siloNavTreeService.getSilos("dummy-country-code", "dummy-device-type", null);
  }
  
  @Test(expected = Exception.class)
  public void testGetInitialMobileSilosThrowException() {
    given(s3SiloNavTreeReader.loadSiloNavTree(anyString())).willThrow(Exception.class);
    siloNavTreeService.getInitialMobileSilos("dummy-country-code", null);
  }
  
  @Test
  @SneakyThrows
  public void testGetInitialMobileSilos() {
    ObjectMapper mapper = new ObjectMapper();
    
    given(s3SiloNavTreeReader.loadSiloNavTree(anyString())).willReturn(mapper.writeValueAsString(createSilos()));
    given(navigationServiceConfig.getIntegration().getCountryCodes()).willReturn(Collections.singletonList("ABC_COUNTRY"));
    
    final Silos actualNavigationCategories = siloNavTreeService.getInitialMobileSilos(UNITED_STATES_COUNTRY_CODE, null);
    
    String expectedNavigationCategoriesAsString = mapper.writeValueAsString(createSilos());
    String actualNavigationCategoriesAsString = mapper.writeValueAsString(actualNavigationCategories);
    
    assertThat(expectedNavigationCategoriesAsString, is(actualNavigationCategoriesAsString));
  }
  
  private Silos createSilos() {
    Silos silos = new Silos();
    getSilosTree(silos, 2);
    return silos;
  }
}

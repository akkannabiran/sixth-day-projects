package com.sixthday.navigation.api.controllers;

import static com.sixthday.navigation.api.data.SiloNavTreeTestDataFactory.getInitialMobileTestSilos;
import static com.sixthday.navigation.api.data.SiloNavTreeTestDataFactory.getTestSilosForDesktop;
import static com.sixthday.navigation.api.data.SiloNavTreeTestDataFactory.getTestSilosForMobile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.MDC;

import com.sixthday.navigation.api.models.SisterSite;
import com.sixthday.navigation.api.models.response.BrandLinks;
import com.sixthday.navigation.api.services.CommonNavService;

import lombok.SneakyThrows;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MDC.class)
public class CommonNavControllerTest {

	@Mock
	private CommonNavService commonNavService;
	
	@InjectMocks
	private CommonNavController commonNavController;

	Map<String,String> navRequestMap = new HashMap<>();

	@Before
	public void setUp() {
		navRequestMap.put("desktopNav", "/silos/US/desktop");
		navRequestMap.put("mobileNav", "/silos/US/mobile");
		navRequestMap.put("mobileNavInitial	", "/silos/US/initial");
		navRequestMap.put("brandLinks", "");
	}



	@Test
	@SneakyThrows
	public void shouldReturnCommonNavResponseForRequestBodyWithNavRequestAndNavPath() {
		Map<String,Object> navOutputMap = new HashMap<>();
		navOutputMap.put("desktopNav", getTestSilosForDesktop());
		navOutputMap.put("mobileNav", getTestSilosForMobile());
		navOutputMap.put("mobileNavInitial", getInitialMobileTestSilos());
		navOutputMap.put("brandLinks", new BrandLinks(Arrays.asList(new SisterSite("name", "url", Arrays.asList(new SisterSite.TopCategory("topCat", "topCatUrl"))))));

		when(commonNavService.getNavDetails(navRequestMap, null)).thenReturn(navOutputMap);

		Map<String,Object> commonNavResponseMaps = commonNavController.getCommonNavResponse(navRequestMap, null);
		assertNotNull(commonNavResponseMaps.get("desktopNav"));
		assertNotNull(commonNavResponseMaps.get("mobileNav"));
		assertNotNull(commonNavResponseMaps.get("mobileNavInitial"));
		assertNotNull(commonNavResponseMaps.get("brandLinks"));

	}
	
	@Test
	@SneakyThrows
	public void shouldReturnEmptyNavResponseWhenExceptionOccures() {
		when(commonNavService.getNavDetails(navRequestMap, null)).thenThrow(new IllegalStateException("This is to test when IllegalStateException been caught inside getCommonNavResponse method"));

		Map<String,Object> commonNavResponseMaps = commonNavController.getCommonNavResponse(navRequestMap, null);
		assertEquals(0,commonNavResponseMaps.size());
	}
	
	@Test
	@SneakyThrows
	public void shouldReturnEmptyNavResponseWhenRequestBodyWithEmptyNavRequestMap() {
		Map<String,Object> navOutputMap = new LinkedHashMap<>();

		when(commonNavService.getNavDetails(new HashMap<>(), null)).thenReturn(navOutputMap);

		Map<String,Object> commonNavResponseMaps = commonNavController.getCommonNavResponse(new HashMap<>(), null);
		assertEquals(0,commonNavResponseMaps.size());
	}
	
	@Test
  @SneakyThrows
  public void shouldReturnCommonNavResponseForGroupBForRequestBodyWithNavRequestAndNavPathAndNavKeyGroupB() {
    Map<String,Object> navOutputMap = new HashMap<>();
    navOutputMap.put("desktopNav", getTestSilosForDesktop());
    navOutputMap.put("mobileNav", getTestSilosForMobile());
    navOutputMap.put("mobileNavInitial", getInitialMobileTestSilos());
    navOutputMap.put("brandLinks", new BrandLinks(Arrays.asList(new SisterSite("name", "url", Arrays.asList(new SisterSite.TopCategory("topCat", "topCatUrl"))))));
    when(commonNavService.getNavDetails(navRequestMap, "B")).thenReturn(navOutputMap);

    Map<String,Object> commonNavResponseMaps = commonNavController.getCommonNavResponse(navRequestMap, "B");
    
    verify(commonNavService).getNavDetails(navRequestMap, "B");
    assertNotNull(commonNavResponseMaps.get("desktopNav"));
    assertNotNull(commonNavResponseMaps.get("mobileNav"));
    assertNotNull(commonNavResponseMaps.get("mobileNavInitial"));
    assertNotNull(commonNavResponseMaps.get("brandLinks"));

  }
	
	@Test
	@SneakyThrows
	public void shouldPutNavKeyGroupInMDCWhenRequestNavKeyGroupIsAvailable() {
	  mockStatic(MDC.class);
	  when(commonNavService.getNavDetails(any(), anyString())).thenReturn(new HashMap<>());
	  
	  Map<String,Object> commonNavResponseMaps = commonNavController.getCommonNavResponse(navRequestMap, "X");
	  
	  assertNotNull(commonNavResponseMaps);
	  verifyStatic(atLeastOnce());
	  MDC.put("NavKeyGroup", "X");
	}
	
}

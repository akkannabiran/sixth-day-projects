package com.sixthday.navigation.api.services;

import static com.sixthday.navigation.api.data.SiloNavTreeTestDataFactory.getInitialMobileTestSilos;
import static com.sixthday.navigation.api.data.SiloNavTreeTestDataFactory.getTestSilosForDesktop;
import static com.sixthday.navigation.api.data.SiloNavTreeTestDataFactory.getTestSilosForMobile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.sixthday.navigation.api.executors.AsyncServiceRequest;
import com.sixthday.navigation.api.models.SisterSite;
import com.sixthday.navigation.api.models.response.BrandLinks;

import lombok.SneakyThrows;

@RunWith(MockitoJUnitRunner.class)
public class CommonNavServiceTest {

	 @Mock
	AsyncServiceRequest asyncServiceRequest;

	 @Mock
	SiloNavTreeService siloNavTreeService;

	 @Mock
	BrandLinksService brandLinksService;

	 @InjectMocks
	CommonNavService commonNavService;
	 
	 Map<String,String> navRequestMap = new HashMap<>();

	@Before
	@SneakyThrows
	public void setUp() {
		navRequestMap.put("desktopNav", "/silos/US/desktop");
		navRequestMap.put("mobileNav", "/silos/US/mobile");
		navRequestMap.put("mobileNavInitial", "/silos/US/initial");
		navRequestMap.put("brandLinks", "");

		when(asyncServiceRequest.createRequest(any()))
		.thenAnswer((Answer) invocation -> {
			Object[] args = invocation.getArguments();
			if (args[0] != null) {
				return CompletableFuture.completedFuture(((Supplier) args[0]).get());
			}
			return CompletableFuture.completedFuture(Optional.empty());
		});

	}

	@Test
	public void shouldReturnCommonNavResponseForRequestBodyIfNavServicesReturnsResponse() {
		when(siloNavTreeService.getSilos(anyString(), anyString(), anyString())).thenReturn(getTestSilosForDesktop());
		when(siloNavTreeService.getSilos(anyString(), anyString(), anyString())).thenReturn(getTestSilosForDesktop());
		when(siloNavTreeService.getInitialMobileSilos(anyString(), anyString())).thenReturn(getInitialMobileTestSilos());
		when(brandLinksService.getBrandLinks()).thenReturn(new BrandLinks(Arrays.asList(new SisterSite("name", "url", Arrays.asList(new SisterSite.TopCategory("topCat", "topCatUrl"))))));
		
		Map<String,Object> commonNavResponseMaps  = commonNavService.getNavDetails(navRequestMap, null);

		assertNotNull(commonNavResponseMaps.get("desktopNav"));
		assertNotNull(commonNavResponseMaps.get("mobileNav"));
		assertNotNull(commonNavResponseMaps.get("mobileNavInitial"));
		assertNotNull(commonNavResponseMaps.get("brandLinks"));
	}
	
	@Test(expected = IllegalStateException.class)
	public void shouldReturnEmptyCommonNavResponseWhenExceptionOccures() {
		when(siloNavTreeService.getSilos(anyString(), anyString(), anyString())).thenThrow(new IllegalStateException("This is to test when IllegalStateException been caught inside getCommonNavResponse method"));
		when(siloNavTreeService.getInitialMobileSilos(anyString(), anyString())).thenThrow(new IllegalStateException("This is to test when IllegalStateException been caught inside getCommonNavResponse method"));
		when(brandLinksService.getBrandLinks()).thenThrow(new IllegalStateException("This is to test when IllegalStateException been caught inside getCommonNavResponse method"));
		
		commonNavService.getNavDetails(navRequestMap, null);

	}
	

	@Test
	public void shouldReturnEmptyCommonNavResponseWhenRequestBodyWithInvalidRequestMap() {
		when(siloNavTreeService.getSilos(anyString(), anyString(), anyString())).thenReturn(null);
		when(siloNavTreeService.getSilos(anyString(), anyString(), anyString())).thenReturn(null);
		when(siloNavTreeService.getInitialMobileSilos(anyString(), anyString())).thenReturn(null);
		when(brandLinksService.getBrandLinks()).thenReturn(null);
		navRequestMap = new HashMap<>();
		navRequestMap.put("invalidNav", "invalidRequest");
		
		Map<String,Object> commonNavResponseMaps  = commonNavService.getNavDetails(navRequestMap, null);

		assertEquals(1,commonNavResponseMaps.size());

	}
	
	@Test
	public void shouldReturnCommonNavResponseOnlyForDesktopForRequestBodyWithOnlyDesktopNav() {
		when(siloNavTreeService.getSilos(anyString(), anyString(), anyString())).thenReturn(getTestSilosForDesktop());
		
		navRequestMap = new HashMap<>();
		navRequestMap.put("desktopNav", "/silos/US/desktop");
		Map<String,Object> commonNavResponseMaps  = commonNavService.getNavDetails(navRequestMap, null);

		assertNotNull(commonNavResponseMaps.get("desktopNav"));
	}
	
	@Test
	public void shouldReturnCommonNavResponseOnlyForMobileForRequestBodyWithOnlyDMobileNav() {
		when(siloNavTreeService.getSilos(anyString(), anyString(), anyString())).thenReturn(getTestSilosForDesktop());
		
		navRequestMap = new HashMap<>();
		navRequestMap.put("mobileNav", "/silos/US/mobile");
		Map<String,Object> commonNavResponseMaps  = commonNavService.getNavDetails(navRequestMap, null);
		

		assertNotNull(commonNavResponseMaps.get("mobileNav"));
	}
	
	@Test
	public void shouldReturnCommonNavResponseOnlyForMobileInitialForRequestBodyWithOnlyMobileInitial() {
		when(siloNavTreeService.getSilos(anyString(),anyString(), anyString())).thenReturn(getInitialMobileTestSilos());
		
		navRequestMap = new HashMap<>();
		navRequestMap.put("mobileNavInitial", "/silos/US/mobile");
		Map<String,Object> commonNavResponseMaps  = commonNavService.getNavDetails(navRequestMap, null);

		assertNotNull(commonNavResponseMaps.get("mobileNavInitial"));
	}
	
	@Test
	public void shouldReturnCommonNavResponseOnlyForBrandlinksForRequestBodyWithOnlyDBrandlink() {
		when(brandLinksService.getBrandLinks()).thenReturn(new BrandLinks(Arrays.asList(new SisterSite("name", "url", Arrays.asList(new SisterSite.TopCategory("topCat", "topCatUrl"))))));
		navRequestMap = new HashMap<>();
		navRequestMap.put("brandLinks", "");
		Map<String,Object> commonNavResponseMaps  = commonNavService.getNavDetails(navRequestMap, null);

		assertNotNull(commonNavResponseMaps.get("brandLinks"));
	}
	
	
	@Test
	public void shouldReturnCommonNavResponseOnlyForDesktopAndMobileForRequestBodyWithOnlyDesktopAndMobileNav() {
		when(siloNavTreeService.getSilos(anyString(), anyString(), anyString())).thenReturn(getTestSilosForDesktop());
		
		navRequestMap = new HashMap<>();
		navRequestMap.put("desktopNav", "/silos/US/desktop");
		navRequestMap.put("mobileNav", "/silos/US/mobile");
		Map<String,Object> commonNavResponseMaps  = commonNavService.getNavDetails(navRequestMap, null);

		assertNotNull(commonNavResponseMaps.get("desktopNav"));
		assertNotNull(commonNavResponseMaps.get("mobileNav"));
	}
	
	@Test
	public void shouldReturnSilosForDesktopWhenRequestBodyWithOnlyDesktopNav() {
		when(siloNavTreeService.getSilos(anyString(), anyString(), anyString())).thenReturn(getTestSilosForDesktop());
		
		navRequestMap = new HashMap<>();
		navRequestMap.put("desktopNav", "/silos/US/desktop");
		String navEndPoint = "/silos/US/desktop";
		Object contents = commonNavService.getNavResponse(anyString(), navEndPoint.split("/"), null);
		Map<String,Object> commonNavResponseMaps  = commonNavService.getNavDetails(navRequestMap, null);

		assertNotNull(contents);
		assertNotNull(commonNavResponseMaps.get("desktopNav"));
	}


	@Test
	public void shouldReturnSilosForMobileWhenRequestBodyWithOnlyMobileNav() {
		when(siloNavTreeService.getSilos(anyString(), anyString(), anyString())).thenReturn(getTestSilosForDesktop());
		
		navRequestMap = new HashMap<>();
		navRequestMap.put("mobileNav", "/silos/US/mobile");
		String navEndPoint = "/silos/US/mobile";
		Object contents = commonNavService.getNavResponse(anyString(), navEndPoint.split("/"), null);
		Map<String,Object> commonNavResponseMaps  = commonNavService.getNavDetails(navRequestMap, null);

		assertNotNull(contents);
		assertNotNull(commonNavResponseMaps.get("mobileNav"));
	}
	
	@Test
	public void shouldReturnSilosForMobileNavInitialWhenRequestBodyWithOnlyMobileNavInitial() {
		when(siloNavTreeService.getSilos(anyString(),anyString(), anyString())).thenReturn(getInitialMobileTestSilos());

		navRequestMap = new HashMap<>();
		navRequestMap.put("mobileNavInitial", "/silos/US/mobile");
		String navEndPoint = "/silos/US/mobile";
		Object contents = commonNavService.getNavResponse(anyString(), navEndPoint.split("/"), null);
		Map<String,Object> commonNavResponseMaps  = commonNavService.getNavDetails(navRequestMap, null);

		assertNotNull(contents);
		assertNotNull(commonNavResponseMaps.get("mobileNavInitial"));
	}
	
	
	public Map<String,Object> getNavOutputMap() {
		Map<String,Object> navOutputMap = new LinkedHashMap<>();
		navOutputMap.put("desktopNav", getTestSilosForDesktop());
		navOutputMap.put("mobileNav", getTestSilosForMobile());
		navOutputMap.put("mobileNavInitial", getInitialMobileTestSilos());
		navOutputMap.put("brandLinks", new BrandLinks(Arrays.asList(new SisterSite("name", "url", Arrays.asList(new SisterSite.TopCategory("topCat", "topCatUrl"))))));
		return navOutputMap;
	}

}

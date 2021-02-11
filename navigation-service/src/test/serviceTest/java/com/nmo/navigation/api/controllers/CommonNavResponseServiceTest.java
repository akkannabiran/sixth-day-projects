package com.sixthday.navigation.api.controllers;

import static com.jayway.restassured.RestAssured.when;
import static com.sixthday.navigation.api.GetCommonNavTreeTestDataFactory.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.ValidatableResponse;
import com.sixthday.navigation.api.models.SisterSite;
import com.sixthday.navigation.api.models.response.BrandLinks;
import com.sixthday.navigation.api.services.CommonNavService;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"navigation.integration.mobile.cron=* * * * * *", "navigation.integration.desktop.cron=* * * * * *"})
public class CommonNavResponseServiceTest {

	@Value("${local.server.port}")
	int port;

	@MockBean
	private RestTemplate restTemplate;

	@InjectMocks
	CommonNavService commonNavService;

	@Before
	public void setup() {
		RestAssured.port = port;
	}

	@Test
	public void shouldReturnCommonNavResponseForRequestBodyWithDesktopMobileAndMobileInitial() {

		Map<String,Object> commonNavResponseMaps  = new HashMap<>();
		commonNavResponseMaps.put("desktopNav", getTestSilosForDesktop());
		commonNavResponseMaps.put("mobileNav", getTestSilosForMobile());
		commonNavResponseMaps.put("mobileNavInitial", getInitialMobileTestSilos());
		commonNavResponseMaps.put("brandLinks", new BrandLinks(Arrays.asList(new SisterSite("name", "url", Arrays.asList(new SisterSite.TopCategory("topCat", "topCatUrl"))))));

		Map<String,String> navRequestMap = new HashMap<>();
		navRequestMap.put("desktopNav", "/silos/US/desktop");
		navRequestMap.put("mobileNav", "/silos/US/mobile");
		navRequestMap.put("mobileNavInitial", "/silos/US/initial");
		navRequestMap.put("brandLinks", "");

		when(commonNavService.getNavDetails(navRequestMap, null)).thenReturn(commonNavResponseMaps);

		when()
		.get("/navigation/silos")
		.then()
		.statusCode(200)
		.body("message", containsString("Unrecognized field \"notARealProperty\""))
		.body("statusCode", is(200));

		ValidatableResponse validatableCommonNavResponse = RestAssured
				.given()
				.contentType("application/json")
				.when()
				.body(new HashMap<>(), ObjectMapperType.JACKSON_2)
				.post("/commonNav")
				.then();
		validatableCommonNavResponse.statusCode(200);
		validatableCommonNavResponse.body("desktopNav", equalTo(1));
		validatableCommonNavResponse.body("mobileNav", equalTo(1));
		validatableCommonNavResponse.body("mobileNavInitial", equalTo(1));
		validatableCommonNavResponse.body("brandLinks", equalTo(1));



	}
}

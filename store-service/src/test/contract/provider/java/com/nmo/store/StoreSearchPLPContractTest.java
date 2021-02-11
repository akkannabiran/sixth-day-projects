package com.sixthday.store;

import au.com.dius.pact.provider.junit.Consumer;
import au.com.dius.pact.provider.junit.PactRunner;
import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactFolder;
import au.com.dius.pact.provider.junit.target.TestTarget;
import au.com.dius.pact.provider.spring.target.MockMvcTarget;
import com.sixthday.store.controllers.StoreSearchController;
import com.sixthday.store.data.StoreDocumentBuilder;
import com.sixthday.store.exceptions.InvalidLocationException;
import com.sixthday.store.models.StoreSearchLocation;
import com.sixthday.store.models.storeindex.StoreDocument;
import com.sixthday.store.repository.ElasticsearchRepository;
import com.sixthday.store.repository.GotWWWRepository;
import com.sixthday.store.services.StoreSearchService;
import com.sixthday.store.util.sixthdayMDCAdapter;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

@RunWith(PactRunner.class)
@Provider("store-service")
@Consumer("plp-svc")
@PactFolder("src/test/contract/resources/pacts")
public class StoreSearchPLPContractTest {

	@TestTarget
	public MockMvcTarget target = new MockMvcTarget();

	@InjectMocks
	private StoreSearchService storeSearchService;

	@Mock
	private sixthdayMDCAdapter sixthdayMDCAdapterMock;

	@Mock
	@SuppressWarnings("unused")
	private ElasticsearchRepository elasticsearchRepository;

	@Mock
	@SuppressWarnings("unused")
	private GotWWWRepository gotWWWRepository;
	
	@InjectMocks
	private StoreSearchController storeSearchController;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		storeSearchController = new StoreSearchController(storeSearchService, sixthdayMDCAdapterMock);
		target.setControllers(storeSearchController);
	}

	@State("EmptyStoreResponse")
	public void getEmptyStoreResponse() {
		when(storeSearchService.getStores("nm",
						new StoreSearchLocation("as"),
						Optional.of(2)))
						.thenReturn(new ArrayList<>());

	}

	@State("ValidStoreResponse")
	public void getStoreNumberListForValidFreeFormSearch() {
		StoreDocument dallasStore1 = new StoreDocumentBuilder().withStoreNumber("1").withStoreName("Dallas - NorthPark").withId("01/NP")
						.build();
		StoreDocument dallasStore2 = new StoreDocumentBuilder().withStoreNumber("2").withStoreName("Fort Worth").withId("02/FW")
						.build();
		when(storeSearchService.getStores("nm",
						new StoreSearchLocation("cityAndStateOrZip"),
						Optional.of(123)))
						.thenReturn((List)Arrays.asList(dallasStore1, dallasStore2));
	}

	@State("InvalidStoreResponse")
	public void getInvalidStoreResponse() {
		when(storeSearchService.getStores("",
						new StoreSearchLocation(""),
						Optional.empty()))
						.thenThrow(new InvalidLocationException("invalid location"));

	}
}

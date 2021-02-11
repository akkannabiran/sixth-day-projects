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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.sixthday.store.config.Constants.Filters.IS_DISPLAYABLE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;


@RunWith(PactRunner.class)
@Provider("store-service")
@Consumer("ECM")
@PactFolder("src/test/contract/resources/pacts")
public class StoreSearchECMContractTest {

    @TestTarget
    public MockMvcTarget target = new MockMvcTarget();
    @InjectMocks
    private StoreSearchService storeSearchService;
    @Mock
    private sixthdayMDCAdapter sixthdayMDCAdapterMock;
    @Mock
    private ElasticsearchRepository elasticsearchRepository;

    @Mock
    private GotWWWRepository gotWWWRepository;
    @InjectMocks
    private StoreSearchController storeSearchController;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        storeSearchController = new StoreSearchController(storeSearchService, sixthdayMDCAdapterMock);
        target.setControllers(storeSearchController);
    }

    @State("ValidStoreIdPass")
    public void toStoreForAValidStoreId() {
        StoreDocument storeDocumentWithEvents = new StoreDocumentBuilder().withStoreNumber("100").withStoreName("St. Louis").withId("100/SL")
                .withAddressLine1("100 Plaza Frontenac").withAddressLine2(null).withPhoneNumber("314-567-9811")
                .withStoreHours("Mon. 10:00AM - 8:00PM,Tue. 10:00AM - 8:00PM,Wed. 10:00AM - 8:00PM,Thu. 10:00AM - 8:00PM,Fri. 10:00AM - 8:00PM,Sat. 10:00AM - 7:00PM,Sun. 12:00PM - 6:00PM")
                .withStoreEventDocument().withEventName("Event name1").withEventDescription("description1").withEventDuration("September 6 - 8, 10am to 5pm")
                .done()
                .withStoreEventDocument().withEventName("Event name2").withEventDescription("description2").withEventDuration("September 6 - 8, 10am to 5pm")
                .done()
                .build();

        when(elasticsearchRepository.getStoresById(eq("100/SL"))).thenReturn(Arrays.asList(storeDocumentWithEvents));
    }

    @State("ValidCoordinates")
    public void toStoresPresentForCoordinates() {
        StoreDocument storeDocumentWithEvents = new StoreDocumentBuilder().withStoreNumber("02").withStoreName("Dallas - NorthPark").withId("02/NP")
                .withAddressLine1("8687 North Central Expressway").withAddressLine2("Suite 400").withPhoneNumber("214-363-8311")
                .withStoreHours("Mon. 10:00AM - 9:00PM,Tue. 10:00AM - 9:00PM,Wed. 10:00AM - 9:00PM,Thu. 10:00AM - 9:00PM,Fri. 10:00AM - 9:00PM,Sat. 10:00AM - 8:00PM,Sun. 12:00PM - 6:00PM")
                .withStoreEventDocument().withEventName("abc").withEventDescription("abc").withEventDuration("4 jun")
                .done()
                .build();
        StoreDocument storeDocumentWithoutEvents = new StoreDocumentBuilder().withStoreNumber("03").withStoreName("Fort Worth").withId("03/FW")
                .withAddressLine1("2100 Green Oaks Road ").withAddressLine2(null).withPhoneNumber("817-738-3581")
                .withStoreHours("Mon. 10:00AM - 7:00PM,Tue. 10:00AM - 7:00PM,Wed. 10:00AM - 7:00PM,Thu. 10:00AM - 7:00PM,Fri. 10:00AM - 7:00PM,Sat. 10:00AM - 7:00PM,Sun. 12:00PM - 5:00PM")
                .build();

        List<String> gotWWWStores = Arrays.asList("02", "03");
        List<String> filteredStoreNumbers = Arrays.asList("02", "03");
        List<StoreDocument> storeDocuments = Arrays.asList(storeDocumentWithEvents, storeDocumentWithoutEvents);

        when(gotWWWRepository.getStores(eq("nm"), any(StoreSearchLocation.class), any(Optional.class))).thenReturn(gotWWWStores);
        when(elasticsearchRepository.filterStoresForStoreNumbers(filteredStoreNumbers,IS_DISPLAYABLE)).thenReturn(storeDocuments);
    }

    @State("InvalidCoordinates")
    public void noStoresPresentForInvalidCoordinates() {

        when(gotWWWRepository.getStores(eq("nm"), any(StoreSearchLocation.class), any(Optional.class))).thenReturn(Collections.emptyList());
        when(elasticsearchRepository.filterStoresForStoreNumbers(Collections.emptyList(),IS_DISPLAYABLE)).thenReturn(Collections.emptyList());
    }

    @State("unWantedRequestParamsForStoreId")
    public void noStoresPresentForUnWantedRequestParamsForSearchWithStoreId() {
    }

    @State("unWantedRequestParamsForCoordinates")
    public void noStoresPresentForUnWantedRequestParamsForSearchWithCoordinates() {
    }

}
